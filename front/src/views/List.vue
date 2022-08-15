<template>
  <div class="home">
    <img alt="Vue logo" src="../assets/logo.png">
    <DisplayList />
  </div>
</template>

<script>
// @ is an alias to /src
import DisplayList from '@/components/DisplayList.vue'

export default {
  name: 'list',
  components: {
    DisplayList
  }
}
</script>
