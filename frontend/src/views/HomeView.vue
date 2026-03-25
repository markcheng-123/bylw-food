<template>
  <section class="home-page">
    <section class="hero-section">
      <div class="hero-inner">
        <div class="hero-left">
          <p class="hero-kicker">本地特色美食论坛</p>
          <h1>发现真实探店，连接本地吃货</h1>
          <p class="hero-sub">不知道吃什么？让 Foodie AI 帮你一键筛选。</p>
          <RouterLink to="/food" class="explore-btn">探索美食</RouterLink>
        </div>

        <div class="hero-right">
          <article class="ai-cabin">
            <header class="ai-cabin-head">
              <span class="ai-dot"></span>
              <strong>Foodie AI</strong>
            </header>

            <div class="ai-message">
              <span class="ai-avatar">AI</span>
              <p>告诉我预算、口味和区域，我马上给你推荐 3 家真实高分店。</p>
            </div>

            <footer class="ai-input-row">
              <input v-model="aiInput" type="text" placeholder="例如：人均80以内，想吃辣，江汉路附近" />
              <button type="button">发送</button>
            </footer>
          </article>
        </div>
      </div>
    </section>

    <section class="banner-section">
      <div class="banner-wrap">
        <el-carousel height="280px" trigger="click" :interval="4200">
          <el-carousel-item v-for="item in banners" :key="item.id">
            <div class="banner-slide" :style="{ backgroundImage: `url(${item.image})` }">
              <div class="banner-mask">
                <h3>{{ item.title }}</h3>
                <p>{{ item.desc }}</p>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </section>

    <section class="hot-section">
      <header class="hot-head">
        <h2>当前最受欢迎的美食内容</h2>
        <RouterLink to="/food">查看更多</RouterLink>
      </header>

      <div class="hot-grid">
        <FoodCard v-for="card in topHotPosts" :key="card.id" :item="card" />
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { fetchFoodPostList, type FoodPostCard } from '@/api/post'
import FoodCard from '@/components/FoodCard.vue'

const aiInput = ref('')
const hotPosts = ref<FoodPostCard[]>([])

const banners = [
  { id: 1, title: '本周精选活动', desc: '夜宵节满减券限时发放中', image: 'https://images.unsplash.com/photo-1526318896980-cf78c088247c?auto=format&fit=crop&w=1400&q=80' },
  { id: 2, title: '城市热门榜更新', desc: '火锅、烧烤、小龙虾榜单发布', image: 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=1400&q=80' },
  { id: 3, title: '商家认证专区', desc: '查看真实资质与主厨信息', image: 'https://images.unsplash.com/photo-1551218808-94e220e084d2?auto=format&fit=crop&w=1400&q=80' },
]

const topHotPosts = computed(() => hotPosts.value.slice(0, 4))

async function loadHotPosts() {
  try {
    const response = await fetchFoodPostList({
      current: 1,
      size: 12,
    })
    hotPosts.value = response.data.records
  } catch {
    hotPosts.value = []
  }
}

onMounted(async () => {
  await loadHotPosts()
})
</script>

<style scoped>
.home-page {
  --bg-cream: #f7f2e9;
  --bg-ivory: #fffdf8;
  --text-main: #3a2a1f;
  --text-sub: #7b6a5b;
  --line: rgba(120, 86, 51, 0.14);
  --brand: #8b4d2b;

  min-height: 100%;
  padding: 24px 28px 40px;
  background: radial-gradient(circle at 20% -10%, rgba(189, 130, 80, 0.12), transparent 45%), var(--bg-cream);
  color: var(--text-main);
}

.hero-section {
  min-height: 450px;
  border-radius: 24px;
  background: linear-gradient(135deg, #fff8ee 0%, #f7f0e4 56%, #f3eadc 100%);
  border: 1px solid var(--line);
  padding: 24px;
}

.hero-inner {
  max-width: 1200px;
  margin: 0 auto;
  min-height: 402px;
  display: grid;
  grid-template-columns: 45% 55%;
  gap: 24px;
  align-items: center;
}

.hero-left {
  display: grid;
  gap: 14px;
  align-content: center;
}

.hero-kicker {
  margin: 0;
  color: #a0856f;
  font-size: 13px;
  letter-spacing: 0.08em;
}

.hero-left h1 {
  margin: 0;
  font-size: clamp(32px, 3vw, 46px);
  line-height: 1.15;
  max-width: 13ch;
}

.hero-sub {
  margin: 0;
  color: var(--text-sub);
  font-size: 17px;
}

.explore-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 128px;
  height: 42px;
  border-radius: 999px;
  border: 1px solid rgba(139, 77, 43, 0.35);
  background: rgba(255, 255, 255, 0.66);
  color: var(--brand);
  font-weight: 600;
  text-decoration: none;
  cursor: pointer;
}

.hero-right {
  display: flex;
  justify-content: center;
  align-items: center;
}

.ai-cabin {
  width: min(560px, 100%);
  background: var(--bg-ivory);
  border-radius: 16px;
  border: 1px solid rgba(132, 94, 62, 0.1);
  padding: 18px;
  box-shadow: 0 24px 60px rgba(67, 41, 19, 0.18), 0 8px 20px rgba(67, 41, 19, 0.08);
  display: grid;
  gap: 16px;
}

.ai-cabin-head {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6c5340;
}

.ai-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: #4caf50;
}

.ai-message {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.ai-avatar {
  width: 30px;
  height: 30px;
  border-radius: 999px;
  background: #f0e3d4;
  color: #7e5738;
  font-size: 12px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.ai-message p {
  margin: 0;
  flex: 1;
  border-radius: 12px;
  background: #f8f4ed;
  border: 1px solid rgba(128, 98, 71, 0.12);
  color: #5f4d3f;
  padding: 11px 12px;
  line-height: 1.55;
}

.ai-input-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
}

.ai-input-row input {
  height: 44px;
  border-radius: 999px;
  border: 1px solid rgba(131, 101, 72, 0.16);
  background: #ffffff;
  padding: 0 16px;
  outline: none;
}

.ai-input-row button {
  height: 44px;
  min-width: 88px;
  border-radius: 999px;
  border: none;
  background: linear-gradient(135deg, #996242, #7f4728);
  color: #fff;
  font-weight: 600;
  cursor: pointer;
}

.banner-section {
  padding-top: 26px;
}

.banner-wrap {
  max-width: 1200px;
  margin: 0 auto;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 16px 34px rgba(66, 43, 24, 0.12);
}

.banner-slide {
  width: 100%;
  height: 280px;
  background-size: cover;
  background-position: center;
  position: relative;
}

.banner-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(100deg, rgba(0, 0, 0, 0.42), rgba(0, 0, 0, 0.12));
  color: #fff;
  display: grid;
  align-content: end;
  gap: 6px;
  padding: 24px 28px;
}

.banner-mask h3,
.banner-mask p {
  margin: 0;
}

.hot-section {
  max-width: 1200px;
  margin: 28px auto 0;
}

.hot-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.hot-head h2 {
  margin: 0;
  font-size: 26px;
}

.hot-head a {
  color: #8b5f3e;
  text-decoration: none;
  font-weight: 600;
}

.hot-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

@media (max-width: 1200px) {
  .hero-inner {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .hot-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .home-page {
    padding: 14px;
  }

  .hero-section {
    min-height: auto;
    padding: 14px;
  }

  .hot-grid {
    grid-template-columns: 1fr;
    gap: 14px;
  }
}
</style>
