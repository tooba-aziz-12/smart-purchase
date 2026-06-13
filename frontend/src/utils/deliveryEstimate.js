const MONTHS = [
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
];

export function formatDeliveryDate(isoDate) {
    const [year, month, day] = isoDate.split("-");
    return `${parseInt(day)} ${MONTHS[parseInt(month) - 1]} ${year}`;
}

export function renderDeliveryEstimate({
    estimatedDelivery,
    estimatedDeliveryRange
} = {}) {
    if (estimatedDelivery) {
        return `Delivery by ${formatDeliveryDate(estimatedDelivery)}`;
    }

    if (estimatedDeliveryRange) {
        return `Delivery ${formatDeliveryDate(estimatedDeliveryRange.from)} – ${formatDeliveryDate(estimatedDeliveryRange.to)}`;
    }

    return "Delivery estimate unavailable";
}
