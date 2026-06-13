import { describe, expect, it } from "vitest";
import { formatPrice } from "./formatPrice.js";

describe("formatPrice", () => {

    it("adds thousands separators for whole rupees", () => {
        expect(formatPrice(14500)).toBe("PKR 14,500");
    });

    it("rounds to whole rupees so paisa is not shown", () => {
        expect(formatPrice(1192.5)).toBe("PKR 1,193");
    });

    it("accepts numeric strings", () => {
        expect(formatPrice("7500")).toBe("PKR 7,500");
    });

    it("falls back gracefully for invalid input", () => {
        expect(formatPrice(undefined)).toBe("PKR —");
    });
});
