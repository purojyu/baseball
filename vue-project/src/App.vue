<template>
  <div id="app">
    <router-view />
    <AppFooter />
  </div>
</template>

<script>
import AppFooter from "./components/AppFooter.vue";

export default {
  name: "App",
  components: {
    AppFooter,
  },
};
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  background-color: #f0f8ff;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 15px;
}

h1 {
  font-size: 32px;
  margin-bottom: 20px;
  color: #0056b3;
}

.header_font {
  font-weight: bold;
  font-size: 24px;
  color: #0056b3;
}

.multiselect {
  max-width: 100%;
  width: 100%;
}

.multiselect__tags {
  border: 2px solid #4e73df !important;
  border-radius: 4px;
}

.multiselect__input,
.multiselect__single {
  border: none;
  background-color: transparent;
}

.multiselect--active .multiselect__tags {
  border-color: #2e59d9 !important;
}

.multiselect__option--highlight {
  background-color: #4e73df;
  color: #fff;
}

.multiselect__option--selected.multiselect__option--highlight {
  background-color: #2e59d9;
}

.search-button {
  width: auto;
  min-width: 120px;
  padding-left: 20px;
  padding-right: 20px;
  background-color: #4e73df;
  border-color: #4e73df;
  transition: all 0.3s ease;
}

.search-button:hover {
  background-color: #2e59d9;
  border-color: #2e59d9;
}

label {
  display: block;
  margin-bottom: 5px;
  color: #0056b3;
  font-weight: 600;
  font-size: 24px !important;
}

.b-table {
  margin-top: 20px;
  border: 1px solid #4e73df;
  border-radius: 4px;
  overflow: hidden;
}

.b-table >>> thead th {
  background-color: #4e73df;
  color: #fff;
  border-color: #4668c5;
}

.b-table >>> tbody tr:nth-child(even) {
  background-color: #e6f0ff;
}

.b-table >>> tbody tr:hover {
  background-color: #d4e4ff;
}

.pitcher-label {
  margin-right: 65px;
}

.batter-label {
  margin-left: 65px;
}

.justify-content-center {
  justify-content: center;
}

.small-text {
  font-size: 16px;
}

.footer-container {
  background-color: #e6f3ff;
  padding: 1rem;
}

.footer {
  max-width: 1200px;
  margin: 0 auto;
}

.footer h4,
.footer .footer-heading {
  margin-bottom: 10px;
  font-size: 1.5rem;
  font-weight: 500;
  line-height: 1.2;
}

.disclaimer p {
  margin: 0;
  font-size: 14px;
  color: #495057;
  text-align: left;
}

.disclaimer-heading {
  margin-top: 20px;
}

.footer a {
  color: #0056b3;
  text-decoration: underline;
}

.footer a:hover {
  text-decoration: none;
}

/* ローディングオーバーレイのスタイル */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  max-width: 90%;
}

.loading-text {
  margin: 0;
  font-size: 1rem;
  color: #0056b3;
  font-weight: bold;
}

.loading-subtext {
  margin: 0;
  font-size: 0.85rem;
  color: #495057;
  text-align: center;
  line-height: 1.5;
  animation: fadeIn 0.4s ease-in;
}

.loading-subtext small {
  color: #6c757d;
  font-size: 0.75rem;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}

.spinner {
  border: 8px solid #f3f3f3;
  border-top: 8px solid #3498db;
  border-radius: 50%;
  width: 50px;
  height: 50px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@media (max-width: 767px) {
  .b-table >>> thead th {
    white-space: nowrap !important;
    vertical-align: middle !important;
    writing-mode: horizontal-tb !important;
    text-orientation: mixed !important;
    font-size: 0.75rem;
    padding: 0.4rem 0.2rem !important;
  }

  .b-table >>> table {
    table-layout: auto;
    width: 100%;
  }

  .b-table >>> .table-responsive {
    overflow-x: auto;
  }

  h1 {
    font-size: 24px;
  }

  .header_font {
    margin: 0;
    text-align: center;
    font-size: 24px;
  }

  label {
    font-size: 20px !important;
  }

  .vs-label {
    font-size: 14px;
  }

  .mt-4.align-items-center .col-4 {
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .pitcher-label,
  .batter-label {
    margin-right: 0;
    margin-left: 0;
  }

  .justify-content-center .col-md-6 {
    max-width: 100%;
  }
}

.mt-3.justify-content-center {
  margin-bottom: 20px;
}

.mt-3.justify-content-center label {
  font-size: 18px;
  margin-bottom: 10px;
}

.mt-3.justify-content-center .multiselect {
  max-width: 200px;
  margin: 0 auto;
}
.search-button:disabled {
  background-color: #a0aec0;
  border-color: #a0aec0;
  cursor: not-allowed;
  opacity: 0.6;
}

.search-button:disabled:hover {
  background-color: #a0aec0;
  border-color: #a0aec0;
}

.search-button:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(50, 50, 93, 0.11), 0 1px 3px rgba(0, 0, 0, 0.08);
}
.alert {
  margin-top: 20px;
  padding: 15px;
  border-radius: 4px;
  font-size: 16px;
}

.alert-danger {
  background-color: #f8d7da;
  color: #721c24;
  border-color: #f5c6cb;
}

@media (max-width: 575px) {
  .container {
    padding: 10px;
  }

  h1 {
    font-size: 20px;
  }

  .vs-label {
    font-size: 12px;
  }

  .search-button {
    min-width: 100px;
    font-size: 14px;
  }

  .mt-3.justify-content-center label {
    font-size: 16px;
  }

  .mt-3.justify-content-center .multiselect {
    max-width: 100%;
  }

  .multiselect {
    max-width: 100%;
    width: 100%;
    font-size: 12px;
    padding: auto;
  }

  .multiselect__input,
  .multiselect__single {
    font-size: 14px;
  }

  .multiselect__tags {
    min-height: 38px;
  }
  .small-text {
    font-size: 12px;
  }
  h1 {
    font-size: 24px !important;
  }

  .disclaimer p {
    margin: 0;
    font-size: 10px;
    color: #6c757d;
    text-align: left;
  }
}
</style>
