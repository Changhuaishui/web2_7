<div class="tag-container">
  <el-tag
    v-for="tag in tags"
    :key="tag"
    :type="selectedTag === tag ? 'primary' : 'primary'"
    :effect="selectedTag === tag ? 'light' : 'plain'"
    class="tag-item"
    @click="filterByTag(tag)"
  >
    {{ tag }}
  </el-tag>
</div> 