import {
    describe,
    expect,
    it
} from "vitest";
import {
    formatDeliveryDate,
    renderDeliveryEstimate
} from "./deliveryEstimate.js";

describe("deliveryEstimate", () => {

    it("formats a single delivery date", () => {
        expect(formatDeliveryDate("2026-06-18")).toBe("18 Jun 2026");
    });

    it("renders a single delivery date", () => {
        expect(
            renderDeliveryEstimate({
                estimatedDelivery: "2026-06-18"
            })
        ).toBe("Delivery by 18 Jun 2026");
    });

    it("renders a delivery range", () => {
        expect(
            renderDeliveryEstimate({
                estimatedDeliveryRange: {
                    from: "2026-06-18",
                    to: "2026-06-21"
                }
            })
        ).toBe("Delivery 18 Jun 2026 – 21 Jun 2026");
    });
});
