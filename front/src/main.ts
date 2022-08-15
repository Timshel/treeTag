import Vue from 'vue'
import App from './App.vue'
import router from './router'
import VueMarkdown from 'vue-markdown'

Vue.config.productionTip = false

new Vue({
  router,
  render: h => h(App),
  components: { VueMarkdown }
}).$mount('#app')
