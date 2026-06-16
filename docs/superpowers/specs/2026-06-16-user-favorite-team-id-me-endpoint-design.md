# Design Spec: Add favoriteTeamId to /users/me Endpoint

## 1. Overview
The `/api/v1/users/me` endpoint currently returns user information using the `UserResponse` DTO, which only contains `id` and `nickname`. We need to add the user's supported/cheered team ID (`favoriteTeamId`) to the `UserResponse` DTO so that frontend clients can retrieve the logged-in user's favorite team directly from this endpoint.

## 2. Requirements & Constraints
- The `/api/v1/users/me` endpoint response body (`UserResponse`) must include the `favoriteTeamId` field.
- If the user has not chosen a favorite team, the field should return `null`.
- Keep changes minimal and focused. Do not modify unrelated codebase entities or endpoints.

## 3. Proposed Changes

### A. DTO Modification
Modify `com.example.pitchboxd.user.dto.response.UserResponse`:
- Add `Long favoriteTeamId` to the record declaration.
- Update `from(User user)` static factory method to pass `user.getFavoriteTeamId()`.

```java
package com.example.pitchboxd.user.dto.response;

import com.example.pitchboxd.user.domain.User;

public record UserResponse(Long id, String nickname, Long favoriteTeamId) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getNickname(), user.getFavoriteTeamId());
    }
}
```

### B. Testing
Update tests in:
- `com.example.pitchboxd.user.presentation.UserControllerTest`
- `com.example.pitchboxd.user.application.UserServiceTest`

Validate that the returned `UserResponse` correctly reflects the `favoriteTeamId` of the created user (both when it is set and when it is null).

## 4. Alternatives Considered
- Returning the entire team object (`TeamResponse`). This was rejected by the user to keep the API payload simple and return only the ID.
- Adding a relationship mapping in Hibernate (`@ManyToOne`). This was rejected to keep database design simple, following DDD aggregate references by ID.
