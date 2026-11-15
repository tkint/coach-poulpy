A short confirmation in the bottom right corner that goes by itself, such as "Exercise saved": `CpToast`. It is a status, never an alert, so it doesn't take the focus; the screen that shows it removes it after a few seconds. A failure that must be read again belongs in an error note on the screen.

| Vue | Classes |
| --- | --- |
| `<CpToast>` | `cp-toast cp-toast-end cp-toast-bottom` around `cp-alert cp-alert-success` |
