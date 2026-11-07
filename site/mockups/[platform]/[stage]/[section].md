---
outline: [2, 2]
prev: false
next: false
---

<script setup>
import { useData } from 'vitepress'
const { params } = useData()
</script>

<MockupPage :platform="params.platform" :stage="params.stage" :section="params.section" />
