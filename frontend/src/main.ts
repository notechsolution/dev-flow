import {createApp} from 'vue';
import '@milkdown/crepe/theme/common/style.css';
import "@milkdown/crepe/theme/frame.css";
import App from './App.vue'
import router from './router'
import {createPinia} from "pinia";

import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap";

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/display.css'
import * as ElIcons from '@element-plus/icons-vue'
import './index.css'

import mavonEditor from 'mavon-editor'
import 'mavon-editor/dist/css/index.css'

import VueDiff from 'vue-diff';
import 'vue-diff/dist/index.css';
import {library} from '@fortawesome/fontawesome-svg-core';
import {faThumbsDown, faThumbsUp} from '@fortawesome/free-solid-svg-icons';
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";

const app = createApp(App)
app.use(router)
app.use(createPinia())
app.use(ElementPlus)
app.use(mavonEditor)
app.use(VueDiff);

const ElIconsData = ElIcons as unknown as Array<() => Promise<typeof import('*.vue')>>
for (const iconName in ElIconsData) {
    app.component(`ElIcon${iconName}`, ElIconsData[iconName])
}

library.add(faThumbsUp, faThumbsDown);
app.component('font-awesome-icon', FontAwesomeIcon);
app.mount('#app');
