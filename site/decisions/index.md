<script setup>
import { data as adrs } from './adrs.data.ts'
</script>

# Decisions

Architecture decision records: each one says what was decided and why, in a paragraph or two. They live in [`docs/adr/`](../docs/adr/), and a new one is added in the same PR as the change it explains. Agents read the ones touching their area before working there.

<table>
  <thead><tr><th>#</th><th>Decision</th><th>Status</th><th>Date</th></tr></thead>
  <tbody>
    <tr v-for="adr in adrs" :key="adr.url">
      <td>{{ adr.number }}</td>
      <td><a :href="adr.url">{{ adr.title }}</a></td>
      <td>{{ adr.status }}</td>
      <td>{{ adr.date }}</td>
    </tr>
  </tbody>
</table>
