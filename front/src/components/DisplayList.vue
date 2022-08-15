<template>
  <div class="list">
    <h1>List</h1>
    <ul>
      <li v-for="item in items" v-bind:key="item.name" >{{item.name}}</li>
      <li v-for="item in items" v-bind:key="item.name" >{{item.name}}</li>
    </ul>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import axios, { AxiosResponse } from 'axios'

interface UserResponse {
  id: string
  name: string
}

@Component
export default class DisplayList extends Vue {
  items: UserResponse[] = []
  protected axios: any
  private url = 'https://jsonplaceholder.typicode.com/users'

  constructor () {
    super()
    this.axios = axios
  }

  mounted () {
    this.$nextTick(() => {
      this.loadItems()
    })
  }

  private loadItems () {
    if (!this.items.length) {
      this.axios.get(this.url).then((response: AxiosResponse) => {
        this.items = response.data
      }, (error: any) => {
        console.error(error)
      })
    }
  }
}
</script>
