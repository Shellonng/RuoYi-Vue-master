<template>
  <div class="app">
    <router-view />
  </div>
</template>

<style scoped>
:global(body) {
  margin: 0;
  font-family: "Manrope", system-ui, -apple-system, sans-serif;
  color: #0f172a;
  background: #f6f8fb;
  min-height: 100vh;
}
:global(*) {
  box-sizing: border-box;
}
.app {
  min-height: 100vh;
}
</style>
