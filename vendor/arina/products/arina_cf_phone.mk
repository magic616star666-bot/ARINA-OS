# ARINA OS reference product.
# Build/reference target: AOSP Cuttlefish x86_64 phone.
# Vivo V2318 becomes a separate device product only after its hardware tree is validated.

$(call inherit-product, device/google/cuttlefish/vsoc_x86_64/phone/aosp_cf.mk)
$(call inherit-product, vendor/arina/config/common.mk)

PRODUCT_NAME := arina_cf_phone
PRODUCT_BRAND := ARINA
PRODUCT_MODEL := ARINA OS Reference
PRODUCT_MANUFACTURER := ARINA

PRODUCT_SYSTEM_DEFAULT_PROPERTIES += \
    ro.arina.version=0.1 \
    ro.arina.brand=ARINA
