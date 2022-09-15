package io.supabase.gotrue.domain

actual class LocalStorage actual constructor() : SupportedStorage {
    override suspend fun getItem(key: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun setItem(key: String, item: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun removeItem(vararg args: Any): String {
        TODO("Not yet implemented")
    }

}