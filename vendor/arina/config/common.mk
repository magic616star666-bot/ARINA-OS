# Common ARINA OS product configuration.

PRODUCT_PACKAGES += \
    ArinaHome

# ARINA visual customization is merged into AOSP/SystemUI at build time.
PRODUCT_PACKAGE_OVERLAYS += \
    vendor/arina/overlay

PRODUCT_COPY_FILES += \
    vendor/arina/config/permissions/privapp-permissions-arina.xml:$(TARGET_COPY_OUT_SYSTEM_EXT)/etc/permissions/privapp-permissions-arina.xml

PRODUCT_PRODUCT_PROPERTIES += \
    persist.sys.arina.manual_controls=true
