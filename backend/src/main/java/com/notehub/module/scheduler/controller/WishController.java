package com.notehub.module.scheduler.controller;

import com.notehub.module.scheduler.model.Wish;
import com.notehub.module.scheduler.util.JsonUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 愿望清单和未来计划 API。
 */
@RestController
@RequestMapping("/api/wishes")
public class WishController {

    private final JsonUtil jsonUtil;

    public WishController(JsonUtil jsonUtil) {
        this.jsonUtil = jsonUtil;
    }

    @GetMapping
    public List<Wish> listWishes() {
        return jsonUtil.loadWishes();
    }

    @PostMapping
    public List<Wish> saveWishes(@RequestBody List<Wish> wishes) {
        for (Wish wish : wishes) {
            if (wish.getId() == null || wish.getId().isBlank()) {
                wish.setId(UUID.randomUUID().toString());
            }
        }
        jsonUtil.saveWishes(wishes);
        return wishes;
    }
}
