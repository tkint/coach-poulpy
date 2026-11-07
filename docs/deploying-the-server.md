# Deploying the server

The server runs on one Linux machine as Docker containers, behind a shared Caddy for HTTPS. Health data must stay in the EU or Switzerland under the GDPR, so pick a host there ([architecture](./architecture.md#accounts-and-security)). The server answers `/health` with its version and serves [the API](./the-api.md) under `/api/v1`. It connects to Postgres and applies its migrations on start ([ADR 0022](./adr/0022-the-servers-schema-is-sqldelight-migrations-on-postgres.md)); `/health` answers 503 while Postgres doesn't. Postgres holds only the schema's version until accounts arrive ([#25](https://github.com/tkint/coach-poulpy/issues/25)).

The setup follows [ADR 0019](./adr/0019-the-server-runs-with-docker-compose-on-one-server.md). Deployment files are in [`deploy/`](https://github.com/tkint/coach-poulpy/tree/main/deploy), and each [release](./releasing.md#the-server) includes a deployment bundle.

## The containers

```mermaid
flowchart LR
    internet(("Internet")) -- "80 and 443" --> caddy
    subgraph edgeproj["edge project"]
        caddy["caddy · shared HTTPS"]
    end
    subgraph appproj["coach-poulpy project"]
        server["server · coach-poulpy-server:8080"]
        postgres[("postgres · postgres-data")]
    end
    caddy -- "edge network" --> server
    server -- "internal network" --> postgres
```

Only Caddy publishes ports. The server reaches Postgres at `postgres:5432` with the `POSTGRES_PASSWORD` from `.env`, and starts once Postgres is healthy. The server joins the external `edge` network using a unique alias and the private `internal` network; Postgres joins only `internal`. The app and edge are separate Compose projects, so stopping the app leaves Caddy running. The shared network is for trusted applications: containers on it can reach each other's app ports.

## What you need

- Linux with Python 3, Docker Engine and Docker Compose.
- About 1 GB of free memory; the server is capped at 512 MB. Images are built in CI, not on the machine.
- An API hostname (`api.coach-poulpy.com`) whose DNS A/AAAA records point to the machine. The landing site and the web app get their own hostnames and containers later ([ADR 0021](./adr/0021-the-landing-site-the-app-and-the-api-have-their-own-hostname.md)).
- SSH and ports 80/443 open (TCP, plus UDP 443 for HTTP/3).

If `ghcr.io/tkint/coach-poulpy-server` is private, log the machine in once with a token that can read packages: `docker login ghcr.io`.

## Deploy

Everything is deployed from GitHub; you don't log in to the machine for it. Set up the **Deploy** workflow once ([Releasing](./releasing.md#deploy-the-server)), with `DOMAIN` and `POSTGRES_PASSWORD` in the `production` environment, then run **Actions > Deploy > Run workflow** with `latest` or a version. Check it with `curl https://api.coach-poulpy.com/health`.

The workflow writes `~/coach-poulpy/.env` from those settings (replaced on every run, so change settings in GitHub, not on the machine), uploads `compose.yaml` and the site file to `~/coach-poulpy`, and the shared Caddy to `~/caddy`. On the machine, `~/caddy/deploy.py` then:

- creates the external `edge` network and starts Caddy if needed,
- pulls and starts the server and Postgres,
- adds the site to Caddy through its admin API, or replaces it if already there.

Caddy keeps the configuration it receives through the API in its `caddy-config` volume and resumes it after a restart; `bootstrap.json` is only used before the first site is added. Other sites on the same Caddy are left alone, and deployments wait for each other. To change the hostname, update `DOMAIN` in GitHub and deploy again. Any branch can be deployed by hand to a `preview` environment, beside production or in its place ([Deploy a pull request](./releasing.md#deploy-a-pull-request)). HTTPS errors are usually caused by DNS or blocked ports; inspect `cd ~/caddy && docker compose logs caddy`.

`POSTGRES_PASSWORD` only takes effect when the Postgres volume is first created; changing it later needs a password change inside Postgres too.

If another Caddy already holds ports 80/443, remove that container once before the first deploy (`docker rm -f <container>`); the shared Caddy requests the certificates again. Never use `docker compose down -v`: it deletes the volumes.

## Backups

Postgres has nothing to back up until accounts arrive. After that, dump it nightly and before deploying a release with new migrations, since an older server refuses a migrated database, and store the dumps off-machine in the EU or Switzerland:

```sh
mkdir -p ~/backups
cd ~/coach-poulpy
docker compose exec -T postgres pg_dump -U coachpoulpy -Fc coachpoulpy > ~/backups/coachpoulpy-$(date +%F).dump
```

Restore with `pg_restore -U coachpoulpy -d coachpoulpy --clean`, fed through `docker compose exec -T postgres`. Back up `.env` and the shared edge's configuration and migration state as well. Losing Caddy's certificate volume requires issuing new certificates.
