# kotlinx.serialization: keep generated serializers for @Serializable classes.
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
