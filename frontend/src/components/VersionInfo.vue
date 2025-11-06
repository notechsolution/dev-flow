<template>
  <div class="version-info" :title="versionTooltip">
    <span class="version-text">v{{ version }}</span>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'VersionInfo',
  data() {
    return {
      version: '0.0.1',
      buildTime: 'unknown',
    };
  },
  computed: {
    versionTooltip() {
      return `DevFlow v${this.version}\n构建时间: ${this.buildTime}\n`;
    }
  },
  mounted() {
    this.fetchVersion();
  },
  methods: {
    async fetchVersion() {
      try {
        const response = await axios.get('/api/version');
        if (response.data) {
          this.version = response.data.version || '0.0.1';
          this.buildTime = response.data.buildTime || 'unknown';
        }
      } catch (error) {
        console.warn('Failed to fetch version info:', error);
        // 使用package.json中的版本作为fallback
        this.version = '0.0.1';
      }
    }
  }
};
</script>

<style scoped>
.version-info {
  position: fixed;
  bottom: 10px;
  right: 10px;
  padding: 4px 12px;
  background-color: rgba(255, 255, 255, 0.9);
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  font-size: 12px;
  color: #666;
  z-index: 1000;
  cursor: help;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.version-info:hover {
  background-color: rgba(255, 255, 255, 1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.version-text {
  font-weight: 500;
  color: #409EFF;
}
</style>
