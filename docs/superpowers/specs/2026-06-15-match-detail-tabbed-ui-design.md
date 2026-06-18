# 2026-06-15 Match Detail Tabbed UI Spec

This document specifies the UI layout, component states, and Tailwind CSS templates for the **Tabbed Match Detail Page** (Option A: Info / Review separation) designed to look premium on mobile and desktop.

---

## 1. Overview & Theme
* **Target Screen**: Match Detail Page (`MatchDetailPage`)
* **Core Philosophy**: Mobile-first, distraction-free tabbed layout.
* **Theme**: Modern Sporty Dark Mode (Sleek slate and neural shades with green/red/blue accents)
* **Tabs**:
  1. **경기 정보 (Match Info)**: Lineups, Formation, Match Stats.
  2. **팬 리뷰 (Fan Reviews)**: User's Evaluation, MOM Stats, Rating Distribution, and Community Reviews.

---

## 2. Layout Structure

```
┌─────────────────────────────────────────────────────────┐
│                     Match Header                        │
│          (Teams, Score, Status, Time, Venue)            │
├─────────────────────────────────────────────────────────┤
│            Sticky Tab Bar: [경기 정보]  [팬 리뷰]        │
├─────────────────────────────────────────────────────────┤
│                                                         │
│   [Tab Content Area]                                    │
│   - Info Tab: Lineups (Soccer Field), Match Stats       │
│   - Review Tab: Rating Card, MOM info, Review Feed      │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 3. Tailwind CSS Mockup (Full Page for Lovable)

Here is the complete Tailwind CSS and HTML mockup for Option A. It includes state control markers, responsive mobile layout utilities, and premium styling.

```html
<div class="min-h-screen bg-neutral-950 text-neutral-100 font-sans antialiased pb-12">
  
  <!-- 1. MATCH HEADER (Full Width, Centered) -->
  <div class="relative overflow-hidden bg-neutral-900 border-b border-neutral-800 py-8 px-4 sm:px-6">
    <!-- Background Pitch/Gradient Glow -->
    <div class="absolute inset-0 bg-gradient-to-b from-emerald-500/10 via-transparent to-transparent opacity-40 pointer-events-none"></div>
    
    <div class="max-w-4xl mx-auto flex flex-col items-center relative z-10 space-y-4">
      <!-- Round & Info -->
      <div class="flex items-center space-x-2 text-xs font-semibold text-emerald-400 bg-emerald-500/10 px-3 py-1 rounded-full">
        <span>2026 K리그1</span>
        <span class="w-1 h-1 bg-emerald-500 rounded-full"></span>
        <span>15R</span>
        <span class="w-1 h-1 bg-emerald-500 rounded-full"></span>
        <span>경기 종료</span>
      </div>

      <!-- Score Board -->
      <div class="flex items-center justify-between w-full max-w-xl py-2">
        <!-- Home Team -->
        <div class="flex flex-col items-center space-y-2 flex-1">
          <div class="w-16 h-16 bg-neutral-800 rounded-2xl flex items-center justify-center border border-neutral-700 shadow-inner">
            <span class="text-3xl">🔴</span>
          </div>
          <span class="text-base font-bold text-neutral-200">FC서울</span>
        </div>

        <!-- Scores -->
        <div class="flex flex-col items-center px-4 space-y-1">
          <div class="flex items-center space-x-6">
            <span class="text-4xl font-black text-neutral-50 tracking-wider">2</span>
            <span class="text-xl font-bold text-neutral-500">:</span>
            <span class="text-4xl font-black text-neutral-50 tracking-wider">1</span>
          </div>
          <span class="text-[10px] text-neutral-500 bg-neutral-800/50 px-2 py-0.5 rounded">2026.06.10 19:00</span>
        </div>

        <!-- Away Team -->
        <div class="flex flex-col items-center space-y-2 flex-1">
          <div class="w-16 h-16 bg-neutral-800 rounded-2xl flex items-center justify-center border border-neutral-700 shadow-inner">
            <span class="text-3xl">🔵</span>
          </div>
          <span class="text-base font-bold text-neutral-200">수원삼성</span>
        </div>
      </div>

      <!-- Venue & Referee -->
      <p class="text-xs text-neutral-400">🏟️ 서울월드컵경기장  •  👤 김우성 주심</p>
    </div>
  </div>

  <!-- 2. STICKY TAB NAVIGATOR -->
  <div class="sticky top-0 z-30 bg-neutral-950/80 backdrop-blur-md border-b border-neutral-800">
    <div class="max-w-2xl mx-auto flex">
      <!-- Active Tab (경기 정보) -->
      <button class="flex-1 text-center py-4 text-sm font-bold border-b-2 border-emerald-500 text-emerald-400 focus:outline-none transition-all">
        📊 경기 정보
      </button>
      <!-- Inactive Tab (팬 리뷰) -->
      <button class="flex-1 text-center py-4 text-sm font-bold border-b border-transparent text-neutral-400 hover:text-neutral-200 focus:outline-none transition-all">
        💬 팬 리뷰
        <span class="ml-1 text-xs bg-neutral-800 text-neutral-400 px-1.5 py-0.5 rounded-full">48</span>
      </button>
    </div>
  </div>

  <!-- 3. TAB CONTENT AREA -->
  <div class="max-w-2xl mx-auto px-4 mt-6">

    <!-- ==================== [TAB 1: 경기 정보 - ACTIVE] ==================== -->
    <div class="space-y-6">
      
      <!-- Formation / Lineup Section -->
      <div class="bg-neutral-900 border border-neutral-800 rounded-2xl p-4 shadow-lg">
        <h3 class="text-sm font-bold text-neutral-400 mb-4 flex items-center">
          🏃 양 팀 선발 라인업
        </h3>
        
        <!-- Interactive Soccer Field Visualizer -->
        <div class="relative w-full aspect-[4/5] bg-emerald-950/40 border border-emerald-800/30 rounded-xl overflow-hidden p-2 flex flex-col justify-between">
          <!-- Field Markings -->
          <div class="absolute inset-0 border border-emerald-700/20 m-2 rounded pointer-events-none"></div>
          <div class="absolute top-1/2 left-0 right-0 h-0.5 bg-emerald-700/20 pointer-events-none"></div>
          <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-24 h-24 border-2 border-emerald-700/20 rounded-full pointer-events-none"></div>
          
          <!-- Home Team (Top Side) -->
          <div class="flex flex-col space-y-4 relative z-10">
            <div class="flex justify-center text-[10px] text-red-400 font-bold bg-red-950/40 px-2 py-0.5 rounded self-center">FC서울 (4-4-2)</div>
            
            <!-- Forward Line -->
            <div class="flex justify-around">
              <div class="flex flex-col items-center">
                <div class="w-8 h-8 rounded-full bg-red-600 border border-red-400 flex items-center justify-center text-xs font-bold shadow-lg">9</div>
                <span class="text-[10px] mt-1 bg-neutral-950/60 px-1 py-0.5 rounded">일류첸코 (7.9)</span>
              </div>
              <div class="flex flex-col items-center">
                <div class="w-8 h-8 rounded-full bg-red-600 border border-red-400 flex items-center justify-center text-xs font-bold shadow-lg">10</div>
                <span class="text-[10px] mt-1 bg-neutral-950/60 px-1 py-0.5 rounded">조영욱 (6.8)</span>
              </div>
            </div>
            
            <!-- Midfield Line -->
            <div class="flex justify-around">
              <div class="flex flex-col items-center">
                <div class="w-8 h-8 rounded-full bg-red-600 border border-red-400 flex items-center justify-center text-xs font-bold shadow-lg">7</div>
                <span class="text-[10px] mt-1 bg-neutral-950/60 px-1 py-0.5 rounded">임상협 (7.5)</span>
              </div>
              <div class="flex flex-col items-center">
                <!-- Highlighted MOM Player -->
                <div class="w-8 h-8 rounded-full bg-yellow-500 border-2 border-yellow-300 flex items-center justify-center text-xs font-bold shadow-lg text-neutral-950 animate-pulse">6</div>
                <span class="text-[10px] mt-1 bg-yellow-500/20 text-yellow-300 border border-yellow-500/30 px-1 py-0.5 rounded font-bold">기성용 (8.4) 👑</span>
              </div>
              <div class="flex flex-col items-center">
                <div class="w-8 h-8 rounded-full bg-red-600 border border-red-400 flex items-center justify-center text-xs font-bold shadow-lg">8</div>
                <span class="text-[10px] mt-1 bg-neutral-950/60 px-1 py-0.5 rounded">팔로세비치 (6.4)</span>
              </div>
            </div>
          </div>
          
          <!-- Away Team (Bottom Side) -->
          <div class="flex flex-col space-y-4 relative z-10 flex-col-reverse">
            <div class="flex justify-center text-[10px] text-blue-400 font-bold bg-blue-950/40 px-2 py-0.5 rounded self-center">수원삼성 (4-3-3)</div>
            
            <!-- Forward Line -->
            <div class="flex justify-around">
              <div class="flex flex-col items-center">
                <div class="w-8 h-8 rounded-full bg-blue-600 border border-blue-400 flex items-center justify-center text-xs font-bold shadow-lg">11</div>
                <span class="text-[10px] mt-1 bg-neutral-950/60 px-1 py-0.5 rounded">김주찬 (6.2)</span>
              </div>
              <div class="flex flex-col items-center">
                <div class="w-8 h-8 rounded-full bg-blue-600 border border-blue-400 flex items-center justify-center text-xs font-bold shadow-lg">9</div>
                <span class="text-[10px] mt-1 bg-neutral-950/60 px-1 py-0.5 rounded">뮬리치 (5.1)</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Match Stats Section -->
      <div class="bg-neutral-900 border border-neutral-800 rounded-2xl p-5 shadow-lg space-y-4">
        <h3 class="text-sm font-bold text-neutral-400 flex items-center">
          📊 주요 경기 팀 스탯
        </h3>
        
        <div class="space-y-4 text-xs">
          <!-- Stat 1: Possession -->
          <div class="space-y-1">
            <div class="flex justify-between font-bold">
              <span>58%</span>
              <span class="text-neutral-400 font-normal">점유율</span>
              <span>42%</span>
            </div>
            <div class="h-2 w-full bg-neutral-800 rounded-full flex overflow-hidden">
              <div class="bg-red-500 h-full" style="width: 58%"></div>
              <div class="bg-blue-500 h-full" style="width: 42%"></div>
            </div>
          </div>

          <!-- Stat 2: Shots -->
          <div class="space-y-1">
            <div class="flex justify-between font-bold">
              <span>14</span>
              <span class="text-neutral-400 font-normal">슈팅 (유효슛)</span>
              <span>8</span>
            </div>
            <div class="h-2 w-full bg-neutral-800 rounded-full flex overflow-hidden">
              <div class="bg-red-500 h-full" style="width: 63.6%"></div>
              <div class="bg-blue-500 h-full" style="width: 36.4%"></div>
            </div>
          </div>

          <!-- Stat 3: Fouls -->
          <div class="space-y-1">
            <div class="flex justify-between font-bold">
              <span>9</span>
              <span class="text-neutral-400 font-normal">파울</span>
              <span>12</span>
            </div>
            <div class="h-2 w-full bg-neutral-800 rounded-full flex overflow-hidden">
              <div class="bg-red-500 h-full" style="width: 42.8%"></div>
              <div class="bg-blue-500 h-full" style="width: 57.2%"></div>
            </div>
          </div>
        </div>
      </div>
    </div>


    <!-- ==================== [TAB 2: 팬 리뷰 - INACTIVE STATE EXAMPLE] ==================== -->
    <!-- (Normally toggled via JS state management in Lovable) -->
    <div class="hidden space-y-6">
      
      <!-- Part A: Dynamic User Rating Card -->
      <div class="bg-neutral-900 border border-neutral-800 rounded-2xl p-6 shadow-xl space-y-6">
        <div class="flex flex-col items-center justify-center text-center space-y-4">
          <span class="text-3xl">🤔</span>
          <h3 class="text-xl font-bold text-neutral-100">이 경기는 어떠셨나요?</h3>
          <p class="text-sm text-neutral-400">당신의 직관 점수와 한줄평을 남겨보세요!</p>
          
          <div class="flex items-center space-x-2 py-2">
            <button class="text-3xl text-neutral-600 hover:text-yellow-400 transition-colors">★</button>
            <button class="text-3xl text-neutral-600 hover:text-yellow-400 transition-colors">★</button>
            <button class="text-3xl text-neutral-600 hover:text-yellow-400 transition-colors">★</button>
            <button class="text-3xl text-neutral-600 hover:text-yellow-400 transition-colors">★</button>
            <button class="text-3xl text-neutral-600 hover:text-yellow-400 transition-colors">★</button>
            <span class="text-lg font-bold text-neutral-400 ml-2">/ 10</span>
          </div>

          <div class="w-full max-w-md">
            <input type="text" placeholder="한줄평을 남겨보세요 (선택사항, 최대 100자)" 
                   class="w-full bg-neutral-850 border border-neutral-700 text-neutral-100 rounded-xl px-4 py-3 text-sm focus:outline-none focus:border-emerald-500 transition-colors" />
          </div>

          <button class="px-6 py-2.5 bg-emerald-500 hover:bg-emerald-600 text-neutral-950 font-bold rounded-xl transition-all shadow-lg shadow-emerald-500/20">
            평가 등록하기
          </button>
        </div>
      </div>

      <!-- Part B: Highlights & MOM Card -->
      <div class="bg-neutral-900 border border-neutral-800 rounded-2xl p-5 shadow-lg space-y-4">
        <h3 class="text-sm font-bold text-neutral-400">👑 경기 최우수 선수 (MOM)</h3>
        
        <div class="flex items-center space-x-4 bg-yellow-500/5 border border-yellow-500/20 rounded-xl p-4">
          <div class="w-12 h-12 rounded-full bg-yellow-500/10 border border-yellow-500/30 flex items-center justify-center text-2xl">👑</div>
          <div class="flex-1">
            <h4 class="text-sm font-bold text-yellow-400">기성용 (MF, FC서울)</h4>
            <p class="text-xs text-neutral-400">오늘 평점 8.4로 단독 MOM 선정!</p>
          </div>
          <div class="text-right">
            <span class="text-xl font-extrabold text-yellow-400">⭐ 8.4</span>
          </div>
        </div>
      </div>

      <!-- Part C: Reviews Feed -->
      <div class="space-y-3">
        <h3 class="text-sm font-bold text-neutral-400">💬 실시간 팬 리뷰</h3>
        
        <div class="space-y-3">
          <!-- Review Item 1 -->
          <div class="bg-neutral-900 border border-neutral-850 rounded-xl p-4 space-y-2">
            <div class="flex justify-between items-center text-xs">
              <div class="flex items-center space-x-2">
                <span class="font-bold text-neutral-200">수호신12</span>
                <span class="text-red-400 bg-red-500/10 px-2 py-0.5 rounded-full font-bold">🔴 서울팬</span>
              </div>
              <span class="font-extrabold text-yellow-400">⭐ 9.0</span>
            </div>
            <p class="text-sm text-neutral-300">"오늘 기성용 중거리 골 들어갈 때 전율 돋았습니다. 직관 오길 잘했네요."</p>
          </div>

          <!-- Review Item 2 -->
          <div class="bg-neutral-900 border border-neutral-850 rounded-xl p-4 space-y-2">
            <div class="flex justify-between items-center text-xs">
              <div class="flex items-center space-x-2">
                <span class="font-bold text-neutral-200">그랑블루_K</span>
                <span class="text-blue-400 bg-blue-500/10 px-2 py-0.5 rounded-full font-bold">🔵 수원팬</span>
              </div>
              <span class="font-extrabold text-neutral-500">⭐ 4.0</span>
            </div>
            <p class="text-sm text-neutral-300">"후반 수비 집중력 무너진 게 아쉽습니다. 다음 라운드는 전술 변화가 있길..."</p>
          </div>
        </div>
      </div>

    </div>

  </div>
</div>
```

---

## 4. Verification & Testing Points for Lovable

1. **Tab Switch State Interaction**: Ensure clicking the "💬 팬 리뷰" button hides the `📊 경기 정보` content div and shows the `💬 팬 리뷰` content div, changing the active classes (active: `border-b-2 border-emerald-500 text-emerald-400`, inactive: `border-b border-transparent text-neutral-400`).
2. **Responsiveness**: Verify that the formation visualizer soccer pitch scaled aspect-ratio works fine on narrow mobile screens (using Tailwind `w-full aspect-[4/5]`).
3. **MOM highlighting**: Ensure the MOM player is highlighted visually in the lineup tab with a crown icon or pulsing effect.
