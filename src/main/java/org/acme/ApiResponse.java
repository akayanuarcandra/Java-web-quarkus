package org.acme;

public record ApiResponse(boolean success, String message, int count) {
}
