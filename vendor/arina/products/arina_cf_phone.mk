# ARINA OS reference product.
# Uses a generic AOSP/Cuttlefish base until the Vivo V2318 device port is validated.

$(call inherit-product, $(SRC_TARGET_DIR)/product/aosp_cf.mk)
$(call inherit-product, vendor/arina/config/common.mk)

PRODUCT_NAME := arina_cf_phone
PRODUCT_DEVICE := arina_cf_phone
PRODUCT_BRAND := ARINA
PRODUCT_MODEL := ARINA OS Reference
PRODUCT_MANUFACTURER := ARINA

PRODUCT_SYSTEM_DEFAULT_PROPERTIES += \
    ro.arina.version=0.1 \
    ro.arina.design=iOS-inspired \
    ro.arina.brand=ARINA
