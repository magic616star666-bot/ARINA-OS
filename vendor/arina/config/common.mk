# Common ARINA OS product configuration.

PRODUCT_PACKAGES += \
    ArinaHome \
    ArinaLockScreen

PRODUCT_COPY_FILES += \
    vendor/arina/config/permissions/privapp-permissions-arina.xml:$(TARGET_COPY_OUT_SYSTEM_EXT)/etc/permissions/privapp-permissions-arina.xml

PRODUCT_PRODUCT_PROPERTIES += \
    persist.sys.arina.manual_controls=true
