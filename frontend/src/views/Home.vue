<template>
    <el-container>
        <el-header class="p-0">
            <el-menu
                :default-active="activeIndex"
                :ellipsis="false"
                class="el-menu-demo"
                mode="horizontal"
                router
                @select="handleSelect"
            >
                <el-menu-item key="0" index="/dashboard">
                    <h2 style="color: #409eff;font-size: 36px">Development Flow</h2>
                </el-menu-item>
                <div class="flex-grow"/>
                <el-menu-item key="1" index="/dashboard">Welcome {{ store.username }}</el-menu-item>
                <el-menu-item v-if="store.username !== '' && store.username !== null" @click="logout">Logout
                </el-menu-item>
            </el-menu>
        </el-header>
        <el-main class="p-0">
            <el-container>
                <el-header class="p-0">
                    <el-image :src="backgroundImg" fit="fill" style="width: 100%; height: 60px;"></el-image>
                </el-header>
                <el-main class="p-0">
                    <el-container>
                        <el-aside width="200px">
                            <el-menu
                                :default-active="activeIndex"
                                :default-openeds="['/requirement/UserStory']"
                                class="el-menu-vertical-demo h-100"
                                router
                                @select="handleSelect"
                            >
                                <el-menu-item key="/dashboard" index="/dashboard">
                                    <template #title>
                                        <el-icon>
                                            <DataAnalysis/>
                                        </el-icon>
                                        <span>Dashboard</span>
                                    </template>
                                </el-menu-item>

                                <el-sub-menu key="/requirement" index="/requirement">
                                    <template #title>
                                        <el-icon>
                                            <DocumentCopy/>
                                        </el-icon>
                                        <span>User Story</span>
                                    </template>
                                    <el-menu-item key="/requirement/UserStoryCreation"
                                                  index="/requirement/UserStoryCreation">
                                        <template #title>
                                            <el-icon>
                                                <Plus/>
                                            </el-icon>
                                            <span>Creation</span>
                                        </template>
                                    </el-menu-item>
                                </el-sub-menu>

                                <el-menu-item v-if="store.role === 'ADMIN' || store.role === 'OPERATOR'"
                                              key="/metering" index="/metering">
                                    <template #title>
                                        <el-icon>
                                            <Money/>
                                        </el-icon>
                                        <span>Metering</span>
                                    </template>
                                </el-menu-item>

                                <el-menu-item v-if="store.role === 'ADMIN' || store.role === 'OPERATOR'"
                                              key="/projectConfig" index="/projectConfig">
                                    <template #title>
                                        <el-icon>
                                            <Tools/>
                                        </el-icon>
                                        <span>Project</span>
                                    </template>
                                </el-menu-item>
                                <el-menu-item v-if="store.role === 'ADMIN' || store.role === 'OPERATOR'"
                                              key="/users" index="/users">
                                    <template #title>
                                        <el-icon>
                                            <svg height="1em" viewBox="0 0 24 24" width="1em"
                                                 xmlns="http://www.w3.org/2000/svg">
                                                <path
                                                    d="M17 19.5c0-1.657-2.239-3-5-3s-5 1.343-5 3m14-3c0-1.23-1.234-2.287-3-2.75M3 16.5c0-1.23 1.234-2.287 3-2.75m12-4.014a3 3 0 1 0-4-4.472M6 9.736a3 3 0 0 1 4-4.472m2 8.236a3 3 0 1 1 0-6a3 3 0 0 1 0 6"
                                                    fill="none" stroke="currentColor"
                                                    stroke-linecap="round" stroke-linejoin="round"
                                                    stroke-width="1.5"></path>
                                            </svg>
                                        </el-icon>
                                        <span>用户管理</span>
                                    </template>
                                </el-menu-item>
                                <el-menu-item v-if="store.role === 'ADMIN' || store.role === 'OPERATOR'"
                                              key="/projects" index="/projects">
                                    <template #title>
                                        <el-icon>
                                            <svg height="1em" viewBox="0 0 24 24" width="1em"
                                                 xmlns="http://www.w3.org/2000/svg">
                                                <path
                                                    d="M3 5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2zm2-1a1 1 0 0 0-1 1v2h16V5a1 1 0 0 0-1-1zm-1 4v10a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1V8zM7 11h5v2H7zm0 4h3v2H7z"
                                                    fill="currentColor"></path>
                                            </svg>
                                        </el-icon>
                                        <span>项目管理</span>
                                    </template>
                                </el-menu-item>
                            </el-menu>
                        </el-aside>
                        <el-main class="p-0">
                            <el-container>
                                <el-main class="p-0">
                                    <el-scrollbar view-class="h-100" wrap-class="h-100">
                                        <router-view/>
                                    </el-scrollbar>
                                </el-main>
                                <el-footer class="h-auto text-center text-sm text-gray-500">
                                    Please carefully screen AI-generated content
                                </el-footer>
                            </el-container>
                        </el-main>
                    </el-container>
                </el-main>
            </el-container>
        </el-main>
    </el-container>
</template>

<script lang="ts">
import {computed, defineComponent, onMounted} from 'vue'
import router from "@/router";
import {useRoute} from "vue-router";
import backgroundImg from '../assets/img/banner_1.png'
import {userStore} from "@/store/user";
import {ChatDotSquare, DataAnalysis, DocumentCopy, HomeFilled, Money, Plus, Tools, User} from "@element-plus/icons-vue";
import userApi from "@/api/user-api";

const store = userStore();

export default defineComponent({
    name: 'LayoutContent',
    components: {Money, Plus, DataAnalysis, ChatDotSquare, HomeFilled, DocumentCopy, User, Tools},
    setup() {
        const route = useRoute()
        const activeIndex = computed(() => route.path)
        const key = computed(() => route.path)
        const handleSelect = (key: string) => {
            router.push({path: key})
        }
        const logout = () => {
            store.logout()
            router.push({path: '/login'})
        }
        onMounted(async () => {
            const loggedInUserResponse = await userApi.getLoggedInUser();
            if (!loggedInUserResponse.data) {
                await router.push({path: '/login'})
            } else {
                store.login(loggedInUserResponse.data.username, loggedInUserResponse.data.role, loggedInUserResponse.data.projectIds)
            }
        })
        return {
            key,
            activeIndex,
            handleSelect,
            backgroundImg,
            store,
            logout
        }
    }
})
</script>

<style>
.flex-grow {
    flex-grow: 1;
}
</style>
