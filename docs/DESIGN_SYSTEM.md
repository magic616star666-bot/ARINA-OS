# ARINA OS — Final Visual & Interaction Design System

## 1. Product identity

ARINA OS should feel premium, calm, precise and highly polished.

Design target:
- Apple-level refinement and interaction quality
- original ARINA branding
- no direct copying of Apple proprietary icons, assets, private frameworks or source code
- visually consistent across Lock Screen, Home, Control Center, Phone, Conference, Settings and system dialogs

Core identity sentence:

> ARINA OS = premium glass depth + calm motion + precise touch + original ARINA intelligence.

---

## 2. Brand

### Name
ARINA OS

### Logo
Use the ARINA logo/mark, never the Apple logo.

### Brand personality
- intelligent
- premium
- calm
- precise
- futuristic without looking gaming-oriented
- minimal rather than flashy

### Avoid
- rainbow neon
- overuse of red/yellow/green
- excessive glow
- hard sci-fi borders everywhere
- dense screens
- tiny labels
- cheap gradients

---

## 3. Color system

ARINA should have a controlled premium blue family.

### Dark base
- Background 950: #050B16
- Background 900: #081426
- Background 850: #0B1A30
- Surface 800: #10233F
- Surface 750: #173052
- Surface 700: #1E3C62

### Light / glass
- Ice 100: #EAF7FF
- Ice 200: #D8F0FF
- Steel 300: #B7D2E9
- Steel 400: #86A9C8
- Steel 500: #5E82A8

### Accent
- ARINA Cyan 500: #35C6FF
- ARINA Blue 500: #4A7DFF
- ARINA Blue 600: #315EDF
- ARINA Deep Blue 700: #2346A8

### Semantic
Semantic colors should be used sparingly:
- Success: #44D6A5
- Warning: #F0C96A
- Critical: #FF6F86

On major system surfaces, default to blue/cyan/ice unless semantic meaning is essential.

---

## 4. Surface and material language

### Base surface
Use deep navy matte backgrounds.

### Glass surface
Use:
- 10–18% white tint
- 12–20% blur
- subtle 1 px inner highlight
- subtle 1 px blue rim
- low-opacity shadow

### 3D depth
ARINA depth should feel soft, not plastic.

Card recipe:
- outer radius: 22–28 dp
- top highlight: 8–12% white
- lower shadow: 18–28% black
- inner rim: 1 dp cyan/steel at low opacity
- elevation: visual equivalent of 8–14 dp

Pressed:
- scale to 0.975
- move down 2–3 dp
- shadow reduces by ~60%
- haptic tick
- return with spring easing

---

## 5. Corner-radius system

- Small control: 12 dp
- Medium control: 16 dp
- Card: 22 dp
- Large card: 26 dp
- Modal sheet: 30 dp
- Pill: 999 dp

Do not mix random corner sizes.

---

## 6. Spacing system

Use 4 dp base grid.

Primary spacing:
- 4
- 8
- 12
- 16
- 20
- 24
- 32
- 40

Screen horizontal padding:
- phone portrait: 16–20 dp

Vertical rhythm:
- section-to-section: 24–32 dp
- title-to-content: 12–16 dp

---

## 7. Typography

Use a modern sans-serif system font stack compatible with Android/AOSP.

Hierarchy:
- Hero: 32–40 sp, semibold
- Screen title: 28–32 sp, semibold
- Section title: 17–20 sp, semibold
- Card title: 15–17 sp, semibold
- Body: 14–16 sp
- Secondary: 12–13 sp
- Micro/status: 10–11 sp

Rules:
- avoid excessive all-caps
- use all-caps only for small system labels
- generous letter spacing only for tiny status text
- body text must remain highly readable

---

## 8. Motion system

ARINA motion should feel soft, elastic and premium.

### Timing
- micro press: 70–110 ms
- small reveal: 160–220 ms
- sheet/modal: 240–320 ms
- full-screen transition: 320–420 ms

### Easing
Use:
- ease-out for reveal
- ease-in for dismiss
- spring for controls/cards
- no linear animation for important UI

### Spring
Target:
- damping: medium-high
- overshoot: subtle
- bounce: 3–6%

### Motion rules
- preserve spatial continuity
- user should understand where content came from
- avoid simultaneous large motion from many directions

---

## 9. Haptics

Haptics should confirm intent, not annoy.

Use:
- light tick: toggles, segmented controls
- medium click: call, merge, primary action
- success pattern: conference merged
- warning pattern: destructive confirm
- no haptic for passive scrolling

---

## 10. System sound language

ARINA sounds:
- short
- soft
- glassy
- low volume by default
- no harsh beeps

Families:
- tap
- toggle
- confirm
- call connect
- call disconnect
- conference merge
- notification
- critical alert

---

## 11. Home Screen

### Layout
- clean wallpaper / dynamic ARINA background
- status region at top
- app grid with generous breathing room
- translucent dock
- smooth page swiping
- optional smart ARINA widget area

### App icons
- original ARINA icon family
- consistent rounded-square silhouette
- subtle depth
- restrained gradients
- no direct Apple icon copies

### Dock
- 4 primary apps maximum by default
- translucent glass
- 24–28 dp radius
- soft blur and shadow

---

## 12. Lock Screen

### Core
- large time
- date below
- ARINA status/assistant line
- notification stack
- flashlight/camera equivalents using ARINA icon set
- swipe-up unlock gesture

### Visual
- wallpaper-driven blur
- strong readability
- minimal chrome

### Notification behavior
- stack expands smoothly
- tap opens app
- swipe exposes actions
- grouped notifications collapse elegantly

---

## 13. Control Center

### Entry
Swipe down from top-right or configurable gesture.

### Structure
- connectivity cluster
- brightness
- volume
- focus
- conference shortcut
- flashlight
- camera
- media

### Style
- large blurred sheet
- modular tiles
- blue/cyan active state
- steel/gray inactive state
- long-press expands tile

### Conference tile
States:
- Ready
- Active
- 2 calls
- Merged
- Muted
- Speaker

---

## 14. Notifications

- grouped by app
- rounded translucent cards
- timestamp subtle
- app icon compact
- expansion with spring
- swipe actions
- clear-all as secondary action
- critical alerts use semantic color only when required

---

## 15. Settings

### Structure
- large title
- search
- grouped settings cards
- profile / ARINA identity header
- consistent disclosure chevrons
- switches aligned right

### Top-level groups
- ARINA Intelligence
- Connectivity
- Phone & Conference
- Display & Motion
- Sound & Haptics
- Privacy
- Security
- Battery
- Storage
- Apps
- System
- About ARINA OS

---

## 16. Switches

### OFF
- steel-gray track
- white/ice thumb
- low elevation

### ON
- ARINA blue/cyan track
- white thumb
- subtle glow

### Motion
- thumb slides with spring
- track color morphs
- light haptic

---

## 17. Buttons

### Primary
- ARINA blue gradient
- white text
- 16–18 dp radius
- depth + highlight

### Secondary
- glass surface
- steel border
- ice text

### Destructive
- semantic red only where necessary

### Press
- 0.975 scale
- 2–3 dp downward movement
- reduced shadow
- haptic click

---

## 18. Dialogs and sheets

Use bottom sheets for contextual actions.

### Bottom sheet
- radius 30 dp top corners
- background blur
- handle indicator
- spring reveal
- swipe-to-dismiss

### Alert
- centered only for high-importance decisions
- concise text
- primary + secondary action

---

## 19. Phone app

ARINA Phone should be minimal and integrated.

Tabs:
- Favorites
- Recents
- Contacts
- Keypad

Call screen:
- large caller identity
- real call state
- timer
- mute
- keypad
- speaker
- add call
- hold
- conference
- end

Use the same ARINA material language.

---

## 20. Conference Hub

Conference is a first-class OS capability.

### Default structure
- 6 contact slots
- live call state
- per-call timer
- hold/resume
- disconnect
- network/call-quality indicators where real data exists
- global mute
- speaker
- merge
- end conference

### Merge behavior
- button disabled until Android/Telecom exposes merge capability
- when available, animate into active state
- on merge success, show a unified conference state

### Visual
- same navy/ice/cyan system
- no unrelated dashboard clutter
- clear participant hierarchy
- active caller gets slightly brighter depth and edge light

---

## 21. ARINA intelligence layer

ARINA assistant surfaces should be system-native.

Possible placements:
- Lock Screen
- Home smart panel
- Control Center shortcut
- search
- call/conference assistant
- Settings

Assistant UI:
- calm glass sheet
- original ARINA glyph
- minimal waveform
- no cartoon avatar required

---

## 22. System gestures

Recommended:
- swipe up: Home
- swipe up + hold: Recents
- swipe from left/right edge: Back
- swipe down top-right: Control Center
- swipe down top-left/center: Notifications
- long press Home area: personalization

Gestures must be configurable for accessibility.

---

## 23. Recents / multitasking

- card stack
- slightly scaled background
- live previews where safe
- swipe up to dismiss
- tap to reopen
- long press for app actions
- smooth depth transitions

---

## 24. Status bar

Keep clean and compact.

Show:
- time
- signal
- network type
- Wi-Fi
- battery
- privacy indicators

Avoid too many decorative icons.

---

## 25. Privacy

ARINA OS should clearly expose privacy state.

Features:
- mic/camera active indicator
- permission history
- one-tap revoke
- clipboard notifications
- background access summary
- per-app network control where supported

---

## 26. Accessibility

Mandatory:
- scalable text
- high-contrast mode
- reduce motion
- reduce transparency
- haptic intensity control
- color-blind-safe semantic states
- minimum touch target 48 dp

---

## 27. Performance targets

Target:
- 60 fps minimum
- 90/120 fps when hardware supports it
- touch response under 100 ms perceived latency
- no heavy blur on every surface simultaneously
- avoid battery-heavy continuous animations

---

## 28. ARINA wallpaper language

Use original wallpapers:
- dark navy depth
- soft blue light
- layered fog/glass
- subtle abstract ARINA geometry
- no copied Apple wallpapers

Dynamic wallpapers may react gently to:
- time
- battery
- charging
- focus mode

---

## 29. Boot experience

Sequence:
1. black/navy screen
2. ARINA logo appears softly
3. subtle cyan edge light
4. logo resolves into lock screen

No long animation.

---

## 30. Final design rule

Every screen must pass these questions:

1. Is it visually calm?
2. Is the hierarchy obvious?
3. Is there enough breathing room?
4. Does every motion explain a state change?
5. Is the accent color controlled?
6. Does it feel like ARINA rather than a copied OS?
7. Can a non-technical user understand it immediately?

If not, simplify.

---

## 31. Design ownership

ARINA OS should be visibly original.

Allowed inspiration:
- premium mobile OS layout discipline
- glass/translucent materials
- spring motion
- gesture navigation
- control-center concepts
- clean system typography

Do not copy:
- Apple logo
- Apple proprietary icons
- Apple wallpapers
- Apple private source code/frameworks
- exact branded assets

ARINA should ultimately look recognizable as ARINA OS.
