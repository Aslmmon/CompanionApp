# CONTENT-001 — Journey Content Engine

Status: Active

Goal:
Evolve the current journey JSON into a richer content model where every journey carries its own atmosphere, emotion, and identity.

This is NOT a UI task.

This is a content architecture task.

---

# Product Philosophy

Every journey should feel handcrafted.

Users should feel they are entering a different emotional experience every day while maintaining the same navigation and interaction patterns.

The UI remains identical.

Only the atmosphere changes.

---

# Journey Principles

Every journey must answer:

1. Who is this person?
2. What happened?
3. What lesson should I remember?
4. What can I do today?
5. What emotion should stay with me after closing the app?

---

# Theme Philosophy

A theme is NOT based on a person.

A theme represents an emotion.

Multiple journeys may share the same theme.

Examples

Desert
Hope
Mercy
Wisdom
Night
Light
Reflection
Victory

Each theme controls

- Accent colors
- Background colors
- Hero illustration
- Decorative assets
- Icons
- Small motion accents

The screen layout never changes.

---

# Theme Definitions

## Desert

Emotion

Courage

Palette

Primary: #B9773C

Secondary: #D8B07C

Surface: #F6EFE4

Illustration

Desert
Mountains
Warm sunset

Used For

- Hamza
- Khalid
- Badr
- Hijrah

---

## Emerald

Emotion

Mercy

Palette

Primary: #2E7D5A

Secondary: #77B88A

Surface: #F2F8F4

Illustration

Palm leaves
Gardens
Nature

Used For

- Khadijah
- Abu Dharr
- Isa

---

## Ocean

Emotion

Hope

Palette

Primary: #3E7CB1

Secondary: #7FB5D8

Surface: #F2F7FB

Illustration

Sea
Sky
Light

Used For

- Yunus
- Zakariyya
- Tawbah

---

## Night

Emotion

Reflection

Palette

Primary: #28334A

Secondary: #5A6A85

Surface: #F5F6FA

Illustration

Moon
Stars
Lantern

Used For

- Tahajjud
- Dhikr
- Ikhlas

---

## Gold

Emotion

Wisdom

Palette

Primary: #C18A2D

Secondary: #E6C76E

Surface: #FFF9EC

Illustration

Books
Geometry
Light

Used For

- Umar
- Luqman
- Dawud

---

# Required Journey Schema

Every journey MUST contain:

```json
{
  "id": "",
  "dayNumber": 1,

  "title": "",
  "subtitle": "",

  "category": "",

  "person": "",

  "emotion": "",

  "theme": "",

  "heroQuote": "",

  "intention": "",

  "durationMinutes": 10,

  "difficulty": "Easy",

  "estimatedReadingMinutes": 8,

  "cover": {
    "type": "illustration",
    "asset": ""
  },

  "steps": [],

  "references": [],

  "tags": []
}
```

---

# Sample Journeys

## Journey 1

```json
{
  "id": "day_001",
  "dayNumber": 1,
  "title": "Bilal ibn Rabah — Freedom of the Soul",
  "subtitle": "A heart devoted to Allah cannot be enslaved by anyone.",
  "category": "Companion",
  "person": "Bilal ibn Rabah",
  "emotion": "Steadfastness",
  "theme": "Desert",
  "heroQuote": "Ahad... Ahad...",
  "intention": "Read today's journey seeking strength during hardship.",
  "durationMinutes": 10,
  "difficulty": "Easy",
  "estimatedReadingMinutes": 8,
  "cover": {
    "type": "illustration",
    "asset": "desert_bilal"
  },
  "tags": [
    "Patience",
    "Faith",
    "Freedom",
    "Tawheed"
  ]
}
```

---

## Journey 2

```json
{
  "id": "day_002",
  "dayNumber": 2,
  "title": "Khadijah — The Heart That Comforted",
  "subtitle": "Sometimes believing in someone changes history.",
  "category": "Women in Islam",
  "person": "Khadijah",
  "emotion": "Mercy",
  "theme": "Emerald",
  "heroQuote": "Allah will never disgrace you.",
  "intention": "Reflect on how you can become a source of peace for someone.",
  "durationMinutes": 10,
  "difficulty": "Easy",
  "estimatedReadingMinutes": 9,
  "cover": {
    "type": "illustration",
    "asset": "emerald_khadijah"
  },
  "tags": [
    "Support",
    "Love",
    "Faith",
    "Compassion"
  ]
}
```

---

## Journey 3

```json
{
  "id": "day_003",
  "dayNumber": 3,
  "title": "Yunus — Hope Inside the Darkness",
  "subtitle": "No darkness is greater than Allah's mercy.",
  "category": "Prophet",
  "person": "Yunus",
  "emotion": "Hope",
  "theme": "Ocean",
  "heroQuote": "There is no deity except You...",
  "intention": "Return to Allah with hope and sincerity today.",
  "durationMinutes": 10,
  "difficulty": "Easy",
  "estimatedReadingMinutes": 8,
  "cover": {
    "type": "illustration",
    "asset": "ocean_yunus"
  },
  "tags": [
    "Repentance",
    "Hope",
    "Mercy",
    "Patience"
  ]
}
```

---

## Journey 4

```json
{
  "id": "day_004",
  "dayNumber": 4,
  "title": "Umar ibn Al-Khattab — A Heart Transformed",
  "subtitle": "The Quran changes hearts before it changes the world.",
  "category": "Companion",
  "person": "Umar ibn Al-Khattab",
  "emotion": "Wisdom",
  "theme": "Gold",
  "heroQuote": "No heart is beyond guidance.",
  "intention": "Read today's journey with an open heart.",
  "durationMinutes": 10,
  "difficulty": "Medium",
  "estimatedReadingMinutes": 9,
  "cover": {
    "type": "illustration",
    "asset": "gold_umar"
  },
  "tags": [
    "Transformation",
    "Justice",
    "Quran",
    "Leadership"
  ]
}
```

---

## Journey 5

```json
{
  "id": "day_005",
  "dayNumber": 5,
  "title": "Night of Dhikr",
  "subtitle": "Quiet hearts remember their Lord.",
  "category": "Islamic Values",
  "person": null,
  "emotion": "Reflection",
  "theme": "Night",
  "heroQuote": "Indeed, in the remembrance of Allah do hearts find rest.",
  "intention": "Slow down and reconnect with Allah before continuing your day.",
  "durationMinutes": 8,
  "difficulty": "Easy",
  "estimatedReadingMinutes": 7,
  "cover": {
    "type": "illustration",
    "asset": "night_dhikr"
  },
  "tags": [
    "Dhikr",
    "Peace",
    "Reflection",
    "Heart"
  ]
}
```

---

# Instructions

Generate future journeys using this schema.

Maintain a consistent tone.

Do not change the schema.

Reuse themes whenever possible.

Focus on emotional consistency rather than visual variety.

Every journey should leave the user with one memorable lesson and one practical action.