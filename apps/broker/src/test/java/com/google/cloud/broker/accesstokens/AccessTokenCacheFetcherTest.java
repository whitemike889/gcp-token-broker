// Copyright 2019 Google LLC
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.cloud.broker.accesstokens;

import java.io.IOException;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.cloud.broker.settings.AppSettings;

public class AccessTokenCacheFetcherTest {

    private static final String GCS = "https://www.googleapis.com/auth/devstorage.read_write";
    private static final String ALICE = "alice@EXAMPLE.COM";

    @BeforeClass
    public static void setupClass() {
        AppSettings.reset();
        AppSettings.setProperty("REMOTE_CACHE", "com.google.cloud.broker.caching.remote.RedisCache");
        AppSettings.setProperty("PROVIDER", "com.google.cloud.broker.accesstokens.providers.MockProvider");
        AppSettings.setProperty("ACCESS_TOKEN_LOCAL_CACHE_TIME", "1234");
        AppSettings.setProperty("ACCESS_TOKEN_REMOTE_CACHE_TIME", "6789");
        AppSettings.setProperty("ENCRYPTION_ACCESS_TOKEN_CACHE_CRYPTO_KEY", "My-Crypto-Key");
    }

    @Test
    public void testComputeResult() {
        AccessTokenCacheFetcher fetcher = new AccessTokenCacheFetcher(ALICE, GCS);
        AccessToken token = (AccessToken) fetcher.computeResult();
        assertEquals(token.getValue(), "FakeAccessToken/Owner=" + ALICE.toLowerCase() + ";Scope=" + GCS);
        assertEquals(token.getExpiresAt(), 999999999L);
    }

    @Test
    public void testFromJSON() {
        AccessTokenCacheFetcher fetcher = new AccessTokenCacheFetcher(ALICE, GCS);
        String json = "{\"expiresAt\": 888888888, \"value\": \"blah\"}";
        AccessToken token;
        try {
            token = (AccessToken) fetcher.fromJson(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertEquals(token.getValue(), "blah");
        assertEquals(token.getExpiresAt(), 888888888L);
    }

    @Test
    public void testGetCacheKey() {
        AccessTokenCacheFetcher fetcher = new AccessTokenCacheFetcher(ALICE, GCS);
        assertEquals(String.format("access-token-%s-%s", ALICE, GCS), fetcher.getCacheKey());
    }

    @Test
    public void testGetLocalCacheTime() {
        AccessTokenCacheFetcher fetcher = new AccessTokenCacheFetcher(ALICE, GCS);
        assertEquals(1234, fetcher.getLocalCacheTime());
    }

    @Test
    public void testGetRemoteCacheTime() {
        AccessTokenCacheFetcher fetcher = new AccessTokenCacheFetcher(ALICE, GCS);
        assertEquals(6789, fetcher.getRemoteCacheTime());
        AppSettings.setProperty("ENCRYPTION_ACCESS_TOKEN_CACHE_CRYPTO_KEY", "My-Crypto-Key");
    }

    @Test
    public void testGetRemoteCacheCryptoKey() {
        AccessTokenCacheFetcher fetcher = new AccessTokenCacheFetcher(ALICE, GCS);
        assertEquals("My-Crypto-Key", fetcher.getRemoteCacheCryptoKey());
    }

}
