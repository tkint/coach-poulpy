---
aside: false
outline: false
prev: false
next: false
---

<script setup>
import { useData } from 'vitepress'
const { params } = useData()
</script>

<MockupPage :stage="params.stage" :device="params.device" :screen="params.screen" />
