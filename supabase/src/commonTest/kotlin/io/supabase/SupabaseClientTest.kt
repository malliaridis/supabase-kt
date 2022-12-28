package io.supabase

import io.supabase.domain.Todo
import io.supabase.gotrue.http.results.SessionResult
import io.supabase.helper.getClient
import io.supabase.postgrest.http.PostgrestHttpResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
internal class SupabaseClientTest {

    @Test
    @Ignore
    fun signIn_should_work() = runTest {
        val supabase = getClient()
        val result = supabase.auth.signIn(email = "my@email.com", password = "password")
        assertTrue(result is SessionResult.Success, "Authentication should succeed")
        assertEquals(result.data, supabase.auth.session(), "Sign in sessions should be equal.")
    }

    @Test
    @Ignore // Test not working yet
    fun rest_request_should_be_authenticated() = runTest {
        val supabase = getClient()
        supabase.auth.signIn(email = "my@email.com", password = "password")

        when (val response = supabase.from<Todo>("todos").select().execute<List<Todo>>()) {
            is PostgrestHttpResponse.Failure -> fail("Response should succeed.")
            is PostgrestHttpResponse.Success ->
                assertTrue(response.body.isNotEmpty(), "TODOs should not be empty.")
        }
    }

    /*

    import { createClient, SupabaseClient } from '../src/index'
    import { DEFAULT_HEADERS } from '../src/lib/constants'

    const URL = 'http://localhost:3000'
    const KEY = 'some.fake.key'

    const supabase = createClient(URL, KEY)

    test('it should throw an error if no valid params are provided', async () => {
        expect(() => createClient('', KEY)).toThrowError('supabaseUrl is required.')
        expect(() => createClient(URL, '')).toThrowError('supabaseKey is required.')
    })

    test('it should not cache Authorization header', async () => {
        const checkHeadersSpy = jest.spyOn(SupabaseClient.prototype as any, '_getAuthHeaders')

        supabase.auth.setAuth('token1')
        supabase.rpc('') // Calling public method `rpc` calls private method _getAuthHeaders which result we want to test
        supabase.auth.setAuth('token2')
        supabase.rpc('') // Calling public method `rpc` calls private method _getAuthHeaders which result we want to test

        expect(checkHeadersSpy.mock.results[0].value).toHaveProperty('Authorization', 'Bearer token1')
        expect(checkHeadersSpy.mock.results[1].value).toHaveProperty('Authorization', 'Bearer token2')
    })

    describe('Custom Headers', () => {
        test('should have custom header set', () => {
            const customHeader = { 'X-Test-Header': 'value' }

            const checkHeadersSpy = jest.spyOn(SupabaseClient.prototype as any, '_getAuthHeaders')
            createClient(URL, KEY, { headers: customHeader }).rpc('') // Calling public method `rpc` calls private method _getAuthHeaders which result we want to test
            const getHeaders = checkHeadersSpy.mock.results[0].value

            expect(checkHeadersSpy).toBeCalled()
            expect(getHeaders).toHaveProperty('X-Test-Header', 'value')
        })

        test('should allow custom Authorization header', () => {
            const customHeader = { Authorization: 'Bearer custom_token' }
            supabase.auth.setAuth('override_me')
            const checkHeadersSpy = jest.spyOn(SupabaseClient.prototype as any, '_getAuthHeaders')
            createClient(URL, KEY, { headers: customHeader }).rpc('') // Calling public method `rpc` calls private method _getAuthHeaders which result we want to test
            const getHeaders = checkHeadersSpy.mock.results[0].value

            expect(checkHeadersSpy).toBeCalled()
            expect(getHeaders).toHaveProperty('Authorization', 'Bearer custom_token')
        })
    })
     */
}
