# Lovable UI Spec: Match Rating Dynamic Card Component

This document contains the visual specification, states, and Tailwind CSS mockups for Lovable to render the **Match Rating Dynamic Card** on the Pitchboxd Match Detail page.

---

## 1. Component Overview
* **Component Name**: `MatchRatingCard`
* **Theme**: Modern Dark Mode (Sporty, Premium)
* **Colors**:
  * Brand Base: Sleek dark gray (`bg-neutral-900`, `text-neutral-100`)
  * Home Fan Rating: Red accent (`#EF4444` / `text-red-500` / `bg-red-500/10`)
  * Away Fan Rating: Blue accent (`#3B82F6` / `text-blue-500` / `bg-blue-500/10`)
  * Neutral Fan Rating: Green accent (`#10B981` / `text-emerald-500` / `bg-emerald-500/10`)

---

## 2. Component States

### State 1: Unevaluated (Before User Rates)
* **Goal**: Maximize user engagement and rating input.
* **Layout**: A clean star slider rating panel prompting user interaction.

#### UI Mockup (Tailwind CSS)
```html
<div class="w-full max-w-2xl mx-auto bg-neutral-900 border border-neutral-800 rounded-2xl p-6 shadow-xl">
  <div class="flex flex-col items-center justify-center space-y-4">
    <span class="text-3xl">🤔</span>
    <h3 class="text-xl font-bold text-neutral-100">이 경기는 어떠셨나요?</h3>
    <p class="text-sm text-neutral-400">당신의 직관 점수와 한줄평을 남겨보세요!</p>
    
    <!-- Star Rating Container (0 to 10 stars or 5 stars slider) -->
    <div class="flex items-center space-x-2 py-4">
      <button class="text-3xl text-neutral-600 hover:text-yellow-400 transition-colors">★</button>
      <button class="text-3xl text-neutral-600 hover:text-yellow-400 transition-colors">★</button>
      <button class="text-3xl text-neutral-600 hover:text-yellow-400 transition-colors">★</button>
      <button class="text-3xl text-neutral-600 hover:text-yellow-400 transition-colors">★</button>
      <button class="text-3xl text-neutral-600 hover:text-yellow-400 transition-colors">★</button>
      <span class="text-lg font-bold text-neutral-400 ml-2">/ 10</span>
    </div>

    <!-- Quick Comment Input -->
    <div class="w-full max-w-md">
      <input type="text" placeholder="한줄평을 남겨보세요 (선택사항, 최대 100자)" 
             class="w-full bg-neutral-800 border border-neutral-700 text-neutral-100 rounded-xl px-4 py-3 text-sm focus:outline-none focus:border-emerald-500 transition-colors" />
    </div>

    <button class="mt-4 px-6 py-2.5 bg-emerald-500 hover:bg-emerald-600 text-neutral-900 font-bold rounded-xl transition-all shadow-lg shadow-emerald-500/20">
      평가 등록하기
    </button>
  </div>
</div>
```

---

### State 2: Evaluated (After User Rates)
* **Goal**: Provide validation/feedback on the user's action and reward them with aggregated fan stats.
* **Layout**: Shows user's personal review details + overall rating + home/away/neutral split + star distribution histogram.

#### UI Mockup (Tailwind CSS)
```html
<div class="w-full max-w-2xl mx-auto bg-neutral-900 border border-neutral-800 rounded-2xl p-6 shadow-xl space-y-6">
  
  <!-- Part 1: User's Own Evaluation Summary -->
  <div class="flex justify-between items-start bg-neutral-800/40 border border-neutral-800 rounded-xl p-4">
    <div class="space-y-1.5">
      <div class="flex items-center space-x-2">
        <span class="text-xs font-bold text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded-full">나의 평가</span>
        <div class="flex text-yellow-400 text-sm">★★★★☆ <span class="text-neutral-300 ml-1">8/10</span></div>
      </div>
      <p class="text-sm text-neutral-200">"기성용 원더골로 짜릿한 극적 승리! K리그 역사에 남을 명경기였습니다."</p>
    </div>
    <button class="text-xs text-neutral-400 hover:text-neutral-200 underline">수정</button>
  </div>

  <!-- Divider -->
  <hr class="border-neutral-800" />

  <!-- Part 2: Aggregate Statistics -->
  <div class="space-y-4">
    <!-- Header: Global Average -->
    <div class="flex justify-between items-baseline">
      <h4 class="text-sm font-bold text-neutral-400 uppercase tracking-wider">전체 팬 경기 평점</h4>
      <div class="flex items-baseline space-x-1">
        <span class="text-3xl font-extrabold text-neutral-100">⭐ 7.8</span>
        <span class="text-sm text-neutral-400">/ 10</span>
      </div>
    </div>

    <!-- Home / Away / Neutral Rating Split Cards -->
    <div class="grid grid-cols-3 gap-3">
      <!-- Home Rating -->
      <div class="bg-red-500/5 border border-red-500/10 rounded-xl p-3 flex flex-col items-center">
        <span class="text-xs text-red-400 font-semibold mb-1">🔴 홈팬 (서울)</span>
        <span class="text-lg font-black text-red-500">9.2</span>
      </div>
      <!-- Away Rating -->
      <div class="bg-blue-500/5 border border-blue-500/10 rounded-xl p-3 flex flex-col items-center">
        <span class="text-xs text-blue-400 font-semibold mb-1">🔵 원정팬 (수원)</span>
        <span class="text-lg font-black text-blue-500">3.4</span>
      </div>
      <!-- Neutral Rating -->
      <div class="bg-emerald-500/5 border border-emerald-500/10 rounded-xl p-3 flex flex-col items-center">
        <span class="text-xs text-emerald-400 font-semibold mb-1">🟢 중립팬</span>
        <span class="text-lg font-black text-emerald-500">8.1</span>
      </div>
    </div>
  </div>

  <!-- Part 3: Rating Distribution Histogram (Letterboxd Style) -->
  <div class="space-y-3 pt-2">
    <span class="text-xs font-bold text-neutral-400 uppercase tracking-wider">평점 분포</span>
    <div class="flex items-end justify-between h-20 px-4 pt-4 border-b border-neutral-800">
      <!-- Bars representing ratings from 1 to 10 -->
      <div class="w-6 bg-neutral-800 rounded-t h-[10%] group relative hover:bg-neutral-700 transition-colors">
        <span class="absolute -top-6 left-1/2 -translate-x-1/2 text-[10px] text-neutral-400 opacity-0 group-hover:opacity-100">10%</span>
      </div>
      <div class="w-6 bg-neutral-800 rounded-t h-[15%] group relative hover:bg-neutral-700 transition-colors"></div>
      <div class="w-6 bg-neutral-800 rounded-t h-[25%] group relative hover:bg-neutral-700 transition-colors"></div>
      <div class="w-6 bg-neutral-800 rounded-t h-[30%] group relative hover:bg-neutral-700 transition-colors"></div>
      <div class="w-6 bg-neutral-800 rounded-t h-[45%] group relative hover:bg-neutral-700 transition-colors"></div>
      <div class="w-6 bg-neutral-800 rounded-t h-[60%] group relative hover:bg-neutral-700 transition-colors"></div>
      <div class="w-6 bg-neutral-800 rounded-t h-[80%] group relative hover:bg-neutral-700 transition-colors"></div>
      <!-- Current User Rating Marker (Star above the bar or distinct color) -->
      <div class="w-6 bg-emerald-500 rounded-t h-[95%] group relative hover:bg-emerald-600 transition-colors">
        <span class="absolute -top-7 left-1/2 -translate-x-1/2 text-[10px] text-emerald-400 font-bold bg-emerald-500/10 px-1 py-0.5 rounded border border-emerald-500/20">내 선택</span>
      </div>
      <div class="w-6 bg-neutral-800 rounded-t h-[70%] group relative hover:bg-neutral-700 transition-colors"></div>
      <div class="w-6 bg-neutral-800 rounded-t h-[40%] group relative hover:bg-neutral-700 transition-colors"></div>
    </div>
    <!-- X-Axis Labels -->
    <div class="flex justify-between px-4 text-[10px] font-bold text-neutral-500">
      <span>1점 (최악)</span>
      <span>5점</span>
      <span>10점 (명경기)</span>
    </div>
  </div>
  
</div>
```
