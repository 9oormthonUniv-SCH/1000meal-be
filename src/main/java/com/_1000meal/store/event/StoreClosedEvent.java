package com._1000meal.store.event;

import java.time.LocalDate;

public record StoreClosedEvent(Long storeId, LocalDate date) {}

