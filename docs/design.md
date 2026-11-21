# CampusSwap: A Campus Second-Hand Exchange App

Xiaoruikang Ding 123115939
Zixuan Liu 123107277

## Introduction

CampusSwap is a university course project: an Android app for college students to exchange second-hand items on campus. Users can post items, browse and favourite items, negotiate exchange proposals through chat, and after a proposal is accepted, arrange an offline handover and confirm completion by both sides.

## Problem and Target Users

### Problem Background

When courses end, students move dormitories, or graduate, they may have items they no longer use but which still have value, such as textbooks, stationery and daily necessities. At the same time, other students may need these items and be willing to exchange their own unused items.

If exchanges are organised through group chats, item information is easily buried by later messages. Users also need repeated communication to confirm what the other side wants, whether the item is still available, and the handover time and place. When the same item is involved in several negotiations at the same time, the exchange conditions may become inconsistent, or the same exchange may be agreed more than once.

CampusSwap addresses these scenarios by providing a central place to display items, negotiate proposals and confirm handovers.

### Target Users

The main target users are college students who want to exchange second-hand items on campus. A user can both post their own items and browse other people's items.

Typical scenarios include:

- After a semester ends, a student wants to exchange textbooks they no longer need for books needed in the next semester.
- Before moving dormitory, a student wants to exchange unused daily items to classmates who need them.
- A student browses campus items and proposes an exchange using one or more of their own items.

The project assumes that users have basic Android phone skills and are willing to arrange the offline handover on campus themselves. The app does not handle payment, delivery or dispute resolution.

## Project Goal and Scope

The goal is to build an Android prototype with persistent data, so that users can complete the full process of "post item - discover item - negotiate proposal - accept proposal - confirm handover".

The plan is to use fictional demo identities on the same device to simulate both sides, with data stored locally. Identity switching is used to show the actions and state changes of different participants.

## Technical Approach

- **Development environment**: Android Studio 2026.1; Gradle 9.6.0, Kotlin 2.2.10; Room 2.8.4, Navigation 2.9.7
- **Test environment**: Pixel 2 XL, Android 11
- **Language and UI**: Kotlin + XML/View
- **Screens and navigation**: single Activity, multiple Fragments, Navigation Component
- **Lists**: RecyclerView + custom adapter
- **Initial data**: when the database is empty (first installation or after clearing app data), the first database access writes the sample items, favourites, conversations, proposals, proposal items and messages from `SampleData`. A normal restart does not import them again
- **Persistence**: items, favourites, conversations, messages and proposals are written to the local Room database. The current demo identity is stored in SharedPreferences, and images selected by the user are stored in the app's private directory
- **UI architecture**: MainActivity uses AppCompatActivity and setContentView to load XML. The five main screens use Fragments. The bottom bar uses Material BottomNavigationView and is connected to the NavController through NavigationUI
- **Code layering**: `model` / `data` / `ui` three layers

### Application Structure and State Management

- `model/`: data classes and enums such as Item, User, Conversation, Proposal, ProposalItem, Exchange, Message and Favourite.
- `data/`: `CampusSwapDatabase`, DAOs, repositories (`object` singletons), `SampleData`, `UserSession` and `ItemImageStorage`.
- `ui/`: Fragments under `screens/`, `adapters/`, `theme/`, `NavigationConfig` and `ItemImageLoader`.

Startup chain: `CampusSwapApplication.onCreate()` initialises `CampusSwapDatabase` and `UserSession` -> `MainActivity` loads `activity_main` -> `NavHostFragment` uses `nav_graph` to host the five main screens -> `BottomNavigationView` is connected to `NavController` through `NavigationUI`.

Data flow: Fragment -> Repository -> DAO -> Room. Repositories use `suspend` functions and `withTransaction` for reads and writes, and Fragments call them in `viewLifecycleOwner.lifecycleScope` and refresh the UI. Room stores items, favourites, conversations, messages, proposals and exchanges. Images are stored in `filesDir/item_images`.

State and lifecycle: screens query Room again when they are created, and UI state is kept inside the Fragment. ViewModel is not used at present. This keeps the layering simple, but the cost is that temporary state in Fragment memory is not restored after rotation or process death, and the user needs to query or fill it again.

## Initial Features

### Item Posting and Management

Users can:

- browse the item list and see images, categories and exchange wishes
- search by title and filter by category
- view item details
- post, edit and withdraw their own items
- favourite other users' items

A posted item contains a title, description, category (enum), wanted item (string) and one image. The image is selected through the system photo picker, and a copy is saved locally.

Only `AVAILABLE` items can be edited or withdrawn by the owner. Items that have entered the agreed handover stage are marked `RESERVED` and cannot be edited or withdrawn, to avoid changes to exchange conditions already confirmed by both sides.

When item content actually changes or the item is withdrawn, the item is updated in the same database transaction and all `PENDING` proposals containing the item are marked `INVALIDATED`. Saving without changes does not increase the revision and does not trigger invalidation. Proposals that are accepted, rejected, withdrawn or superseded keep their original status.

### Chat and Editing Exchange Conditions

- Users can start a new Chat from another user's item detail page or the home page.
- When starting a new conversation, the initiator needs to edit a Proposal, add several of their own posted items, and select some items from the other side's inventory. The Proposal also includes the exchange time and place (Eircode). After confirming, the Proposal is locked and sent to the other side.
- The other side can accept, reject, or modify the Proposal, lock it and send it back to the requester.
- Both sides can continue the conversation during this period.
- An invalid proposal in the chat is shown as `Exchange proposal - Invalid`. The top of the proposal detail shows the same status and the message `One or more items have been modified or withdrawn since this proposal was created.`
- An invalid proposal is read-only and cannot be accepted or directly edited and resent. Both sides can select items again from "Create Proposal" in the chat and send a new proposal.
- The above steps can be repeated until one side agrees. Then an Exchange record is created and both sides wait for the offline handover.

### Proposal Versions and Item Reservation

- **When sending a Proposal**: that version fixes the list of item IDs, the provider and revision at sending time, and the handover time and place. Items in the proposal can still take part in other negotiations and are not reserved at this point.
- **When displaying a Proposal**: the current Item is read by item ID, and the latest title, image and status are shown. Tapping the image opens the current item detail. Withdrawn, reserved or exchanged items remain in the proposal list and show their status. Historical proposals also read the latest item information. Item content snapshots from sending time are not displayed.
- **When editing or withdrawing an item**: all related `PENDING` proposals become `INVALIDATED` in the same transaction. Other final states do not change. If the item becomes available again later, old proposals do not become valid again.
- **When modifying a Proposal**: a new version is created and sent based on the old version. The old version is kept as history but can no longer be accepted. Only the latest pending version can be accepted.
- **When accepting a Proposal**: check that the proposal is still the latest `PENDING`, then check again that all items in both sides' lists are still `AVAILABLE`, and that ownership and revision match the records at sending time. If all checks pass, in the same database transaction mark the Proposal `ACCEPTED`, create an Exchange with status `AWAITING_HANDOVER`, reserve all related items (set them to `RESERVED`), and mark other pending proposals containing any of these items as invalid. If a check fails, the proposal is not accepted and no items are reserved.

### Confirming Completion

After the offline handover, both sides tap "Confirm handover" on the exchange detail page, and all related items are marked as exchanged.

### Profile Page

Shows the current demo identity and provides:

- My posted items -> detail, edit, withdraw
- My favourites
- Demo identity switching
- About the app

## Navigation Structure

```
MainActivity
- Browse
    - top category filter -> recent items or selected category item list -> item detail
- Search
    - top search box -> enter keyword -> search result list -> item detail
- List
    - post item
- Chats
    - conversation list -> chat detail
        - text messages and historical proposal cards
    - edit exchange list, time and Eircode -> send Proposal
    - accept, reject, withdraw or send a modified version
    - accepted proposal -> Exchange detail, cancel or confirm handover
- Profile
    - my posted items -> detail, edit, withdraw
    - my favourites -> item detail
    - switch demo identity
    - about
```

## Data Model

### Modelling Scope and Conventions

Two users exchange items, and each side provides at least one item. The same pair of users shares one Conversation, which keeps chat messages, multiple proposal versions and multiple historical exchanges.

**Proposal is responsible for whether a proposal is accepted. Exchange is responsible for the handover after acceptance. An Exchange is created only when a Proposal is accepted.** A conversation has at most one `PENDING` proposal or one `AWAITING_HANDOVER` exchange, and the two cannot exist at the same time. Chatting is allowed during the handover period, but a new proposal cannot be sent.

Proposal edit content is kept temporarily in Fragment memory variables. No draft is saved to the database, and it is not guaranteed to be restored after rotation or process restart. Only when sending are the item links, the provider and revision at sending time, and the handover time and place saved. These proposal fields are not modified in place after sending. After modification and resending, the previous pending version becomes `SUPERSEDED`. Item display content always comes from the current Item and is not part of immutable proposal history.

All entity primary keys and corresponding foreign keys are stored as String. New records created at runtime use UUID by default, while `SampleData` uses fixed string IDs for demo convenience (such as `114514-1` and `conversation-alice-bob`), which do not need to be in UUID format. Long is used only for UTC millisecond timestamps, and Int is used for versions. Enums are stored in the database as stable strings.

### User (Demo User)

| Field | Type | Description |
| --- | --- | --- |
| id | String | UUID primary key |
| name | String | User nickname |

### Item

| Field | Type | Description |
| --- | --- | --- |
| id | String | UUID primary key |
| ownerId | String | Foreign key -> `User.id`; keeps the original poster after the exchange |
| title / description | String | Title, description |
| category | ItemCategory | Item category enum |
| wantedDescription | String | Used for card display; short text is recommended |
| imagePath | String | Local image copy path or a persistable reference |
| status | ItemStatus | `AVAILABLE` / `RESERVED` / `EXCHANGED` / `WITHDRAWN` |
| revision | Int | Starts at 1; increases when content actually changes or the item is withdrawn, not on unchanged saves; used to check against the revision recorded when a proposal was sent; reserve, unreserve and complete use status updates and do not increase the revision |
| createdAt / updatedAt | Long | UTC millisecond timestamps |

### Favourite

| Field | Type | Description |
| --- | --- | --- |
| userId | String | Foreign key -> `User.id` |
| itemId | String | Foreign key -> `Item.id`; together with `userId` forms the composite primary key |
| createdAt | Long | Favourite time |

### Conversation

| Field | Type | Description |
| --- | --- | --- |
| id | String | UUID primary key |
| userLowId / userHighId | String | Both reference `User.id`; stored in normalised string order; the two users must be different |
| createdAt | Long | Creation time |

### Proposal (Sent Proposal Version)

| Field | Type | Description |
| --- | --- | --- |
| id | String | UUID primary key |
| conversationId | String | Foreign key -> `Conversation.id` |
| version | Int | Increases from 1 within the same conversation |
| proposerId | String | Foreign key -> `User.id`; must be the initiator |
| receiverId | String | Foreign key -> `User.id` |
| status | ProposalStatus | `PENDING` / `SUPERSEDED` / `ACCEPTED` / `REJECTED` / `WITHDRAWN` / `INVALIDATED` |
| meetingAt | Long | Agreed handover time; must be in the future when sent |
| eircode | String | Handover location Eircode; in the first version it is only input and displayed, with no online address validation |
| createdAt | Long | Sending time; the list, time and place are not modified in place after sending |

### Exchange (Agreed Exchange)

| Field | Type | Description |
| --- | --- | --- |
| id | String | UUID primary key |
| conversationId | String | Foreign key -> `Conversation.id`; must match the conversation of the referenced Proposal |
| proposalId | String | Foreign key -> `Proposal.id`, with a unique index; fixed to the finally accepted proposal and does not follow the latest proposal |
| status | ExchangeStatus | `AWAITING_HANDOVER` / `CANCELLED` / `COMPLETED` |
| lowConfirmedAt / highConfirmedAt | Long? | Confirmation times of the two participants in the conversation; null if not confirmed |
| createdAt / updatedAt | Long | Creation and update times |

### ProposalItem (Proposal Item Link and Sending-Time Check Information)

| Field | Type | Description |
| --- | --- | --- |
| proposalId | String | Foreign key -> `Proposal.id` |
| itemId | String | Foreign key -> `Item.id`; together with `proposalId` forms the composite primary key |
| ownerIdSnapshot | String | Foreign key -> `User.id`; the side that provided the item when sent |
| itemRevision | Int | `Item.revision` at sending time |
| titleSnapshot / descriptionSnapshot | String | Item title and description at sending time |
| categorySnapshot | String | Item category at sending time |
| imagePathSnapshot | String | Item image path at sending time |

### Message (Chat Message, Image and Proposal Card)

| Field | Type | Description |
| --- | --- | --- |
| id | String | UUID primary key |
| conversationId | String | Foreign key -> `Conversation.id` |
| senderId | String | Foreign key -> `User.id`; must belong to the conversation |
| type | MessageType | `TEXT` / `PROPOSAL` / `IMAGE` |
| text | String? | Non-blank for `TEXT`; empty for `PROPOSAL` |
| proposalId | String? | Foreign key -> `Proposal.id`; required for `PROPOSAL`, empty for `TEXT` |
| createdAt | Long | Sending time |
| imagePath | String? | Local image copy path for `IMAGE`; empty for other types |

### State Transitions

```
ItemStatus
AVAILABLE -> RESERVED -> EXCHANGED
AVAILABLE -> WITHDRAWN
RESERVED -> AVAILABLE (Exchange cancelled)
WITHDRAWN -> AVAILABLE (future: relist after editing)

ProposalStatus
PENDING -> ACCEPTED / REJECTED / WITHDRAWN / INVALIDATED / SUPERSEDED
INVALIDATED: the item was edited or withdrawn, or the item was reserved because another proposal was accepted. The UI shows Invalid.
INVALIDATED does not automatically return to PENDING. The proposal must be sent again.
ACCEPTED is the final state of a proposal and does not change to CANCELLED, COMPLETED or INVALIDATED.

ExchangeStatus
created as AWAITING_HANDOVER
-> CANCELLED (neither side has confirmed)
-> COMPLETED (both sides have confirmed)
```

## Limitations and Known Issues

- **Proposal edit drafts are not persisted**: the item list, handover time and Eircode being edited are only kept in Fragment memory variables and are written to the database only after sending. After leaving the page, the draft cannot be restored and must be created again.
- **Temporary state after rotation/process restart is not fully restored**: the selected image when posting or editing an item, the item selection and time/place in proposal editing, etc. only exist in Fragment memory. After screen rotation or process death, they need to be selected or filled again.
- **Image files are not cleaned up**: after a user-selected image is copied to `filesDir/item_images`, old files are not deleted when the image is replaced or the item is withdrawn. The database only stores the path, so old files continue to take up storage space.

## Testing

- Main flows: Browse category filtering and detail; Search and empty results; List posting/editing/withdrawing (including images); Chats text/image messages and Proposal creation/modification/rejection/withdrawal; Profile favourites, my items, identity switching and About.
- Key exceptions: empty forms; editing or withdrawing another user's or a `RESERVED` item; Proposal with an empty list, past time, empty Eircode, or changed items; no identity selected; rotation/process restart; empty states of each list.

## Features Not Included in the First Version

The following features are listed as future improvements:

- user reviews
- account registration
- real-time chat
- payment, deposit and price difference
- preset campus exchange locations
- reporting, dispute resolution and admin backend
- real-time location
- smart recommendations

## Division of Work

### Xiaoruikang Ding (Git commit name: Senri)

- Browse page staggered-grid list and MaterialCardView cards; list reuse in My Items and My Favourites
- Search page and search logic
- Profile page and demo user switching logic
- ListItem page and item posting, editing and withdrawal logic
- Proposal edit/view pages and sending, modification and invalidation logic

### Zixuan Liu

- Screen navigation relationships, bottom navigation bar and Fragment framework
- Item detail page and favourite logic
- Chats page and chat logic (conversation list, chat detail)
- Exchange page and confirmation logic

### Shared Work

- Project problem background
- Basic data structures
- Design document
- Dummy data for testing
- DAO and data persistence
- Image persistence logic
- Testing

## Final Demo Flow

1. Identity A posts several second-hand items.
2. Switch to identity B, search for and view these items.
3. B starts a conversation with A.
4. B adds items from both sides to the exchange list, fills in the time and Eircode, and sends a Proposal. You can demonstrate A modifying it and sending a new version, with the old version becoming `SUPERSEDED`.
5. Switch to the responder of the latest proposal, accept the Proposal, and create an Exchange.
6. Show that both sides' items are reserved and the handover arrangement.
7. Both sides confirm separately, and the exchange is completed.
8. User C has a `PENDING` proposal involving the same items. After A and B accept the proposal and reserve the items, it becomes `INVALIDATED`.
9. Before accepting, edit or withdraw one of the items and show that all related pending proposals become Invalid. Reopen the proposal to see the latest title, image and item status, and the invalidation message. Rejected, withdrawn and accepted historical proposals are not affected.
10. Save the item without changes and confirm that the pending proposal is still valid. Try to edit or withdraw a `RESERVED` item and confirm that the operation is blocked.
