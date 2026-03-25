<template>
  <section class="comment-board">
    <div class="board-head">
      <div>
        <p class="eyebrow">Community</p>
        <h3>评论区</h3>
      </div>
      <span class="comment-count">{{ comments.length }} 条主评论</span>
    </div>

    <form class="composer" @submit.prevent="submitComment()">
      <div v-if="replyTarget" class="reply-banner">
        <span>回复 {{ replyTarget.authorNickname }}</span>
        <button type="button" @click="replyTarget = null">取消</button>
      </div>
      <textarea
        v-model.trim="draft"
        rows="4"
        placeholder="写下你的真实感受、推荐理由或者排队避坑建议吧"
      />
      <div class="composer-foot">
        <p class="hint">支持一级评论和楼中回复，发布后会立即刷新。</p>
        <button class="primary-btn submit-btn" type="submit" :disabled="submitting">
          {{ submitting ? '发布中...' : replyTarget ? '回复评论' : '发布评论' }}
        </button>
      </div>
    </form>

    <div v-if="errorText" class="error-box">{{ errorText }}</div>

    <div v-if="comments.length" class="comment-list">
      <article v-for="item in comments" :key="item.id" class="comment-card">
        <div class="comment-main">
          <div class="avatar">{{ item.authorNickname.slice(0, 1) }}</div>
          <div class="comment-content">
            <div class="comment-meta">
              <strong>{{ item.authorNickname }}</strong>
              <span>{{ formatDate(item.createdAt) }}</span>
            </div>
            <p>{{ item.content }}</p>
            <button class="reply-btn" type="button" @click="replyTarget = item">回复</button>
          </div>
        </div>

        <div v-if="item.replies.length" class="reply-list">
          <article v-for="reply in item.replies" :key="reply.id" class="reply-card">
            <div class="reply-head">
              <strong>{{ reply.authorNickname }}</strong>
              <span>{{ formatDate(reply.createdAt) }}</span>
            </div>
            <p>{{ reply.content }}</p>
            <button class="reply-btn" type="button" @click="replyTarget = reply">回复</button>
          </article>
        </div>
      </article>
    </div>

    <div v-else class="empty-box">
      <strong>还没有评论</strong>
      <p>这个帖子已经准备好被认真讨论了，成为第一个留言的人吧。</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { createComment, fetchComments, type CommentItem } from '@/api/comment'

const props = defineProps<{
  postId: number
}>()

const comments = ref<CommentItem[]>([])
const draft = ref('')
const replyTarget = ref<CommentItem | null>(null)
const submitting = ref(false)
const errorText = ref('')

const emit = defineEmits<{
  refreshed: [count: number]
}>()

function formatDate(value: string) {
  return value.replace('T', ' ').slice(0, 16)
}

async function loadComments() {
  const response = await fetchComments(props.postId)
  comments.value = response.data
  emit('refreshed', countAllComments(response.data))
}

function countAllComments(items: CommentItem[]) {
  return items.reduce((total, item) => total + 1 + item.replies.length, 0)
}

async function submitComment() {
  if (!draft.value) {
    errorText.value = '请先填写评论内容'
    return
  }

  try {
    submitting.value = true
    errorText.value = ''
    await createComment(props.postId, {
      content: draft.value,
      parentId: replyTarget.value?.id,
    })
    draft.value = ''
    replyTarget.value = null
    await loadComments()
  } catch (error) {
    errorText.value = error instanceof Error ? error.message : '评论发布失败'
  } finally {
    submitting.value = false
  }
}

onMounted(loadComments)
</script>

<style scoped>
.comment-board {
  padding: 26px;
  border: 1px solid var(--line);
  border-radius: var(--radius-xl);
  background: rgba(255, 252, 246, 0.92);
  box-shadow: var(--shadow);
}

.board-head,
.composer-foot,
.comment-main,
.comment-meta,
.reply-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.board-head {
  align-items: end;
}

.board-head h3 {
  margin: 0;
  font-size: 28px;
}

.comment-count,
.hint,
.comment-meta span,
.reply-head span,
.empty-box p {
  color: var(--muted);
}

.composer {
  margin-top: 22px;
  padding: 18px;
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.66);
}

.reply-banner {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(189, 91, 47, 0.12);
  color: var(--brand-deep);
}

.reply-banner button,
.reply-btn {
  border: none;
  background: transparent;
  color: var(--brand-deep);
}

.composer textarea {
  width: 100%;
  min-height: 120px;
  padding: 14px 16px;
  border: 1px solid rgba(98, 61, 27, 0.14);
  border-radius: 18px;
  background: #fffdfa;
  font: inherit;
  resize: vertical;
}

.composer-foot {
  align-items: center;
  margin-top: 14px;
}

.hint {
  margin: 0;
}

.submit-btn {
  min-width: 124px;
}

.error-box,
.empty-box {
  margin-top: 18px;
  padding: 18px;
  border-radius: var(--radius-lg);
}

.error-box {
  background: rgba(177, 63, 40, 0.12);
  color: #9e3119;
}

.empty-box {
  background: rgba(255, 255, 255, 0.68);
}

.comment-list {
  display: grid;
  gap: 16px;
  margin-top: 20px;
}

.comment-card {
  padding: 20px;
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(98, 61, 27, 0.08);
}

.comment-main {
  align-items: start;
}

.avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--brand), var(--brand-deep));
  color: #fff;
  font-weight: 700;
}

.comment-content {
  flex: 1;
}

.comment-content p,
.reply-card p {
  margin: 10px 0 0;
  line-height: 1.75;
}

.reply-list {
  display: grid;
  gap: 12px;
  margin-top: 16px;
  margin-left: 54px;
}

.reply-card {
  padding: 16px;
  border-radius: 18px;
  background: rgba(246, 239, 229, 0.92);
}

@media (max-width: 900px) {
  .board-head,
  .composer-foot,
  .comment-main,
  .comment-meta,
  .reply-head {
    flex-direction: column;
  }

  .reply-list {
    margin-left: 0;
  }

  .comment-board {
    padding: 22px;
  }
}
</style>
