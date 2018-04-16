package com.adamratzman.spotify.endpoints.pub.tracks

import com.adamratzman.spotify.main.SpotifyAPI
import com.adamratzman.spotify.utils.*
import java.util.function.Supplier
import java.util.stream.Collectors

/**
 * Endpoints for retrieving information about one or more tracks from the Spotify catalog.
 */
class TracksAPI(api: SpotifyAPI) : SpotifyEndpoint(api) {
    /**
     * Get Spotify catalog information for a single track identified by its unique Spotify ID.
     *
     * @param trackId The Spotify ID for the track.
     * @param market Provide this parameter if you want to apply [Track Relinking](https://github.com/adamint/spotify-web-api-kotlin/blob/master/README.md#track-relinking)
     *
     * @throws BadRequestException if [trackId] cannot be found
     */
    fun getTrack(trackId: String, market: Market? = null): SpotifyRestAction<Track> {
        return toAction(Supplier {
            get("https://api.spotify.com/v1/tracks/${trackId.encode()}${if (market != null) "?market=${market.code}" else ""}").toObject<Track>(api)
        })
    }

    /**
     * Get Spotify catalog information for multiple tracks based on their Spotify IDs.
     *
     * @param trackIds The Spotify ID for the tracks.
     * @param market Provide this parameter if you want to apply [Track Relinking](https://github.com/adamint/spotify-web-api-kotlin/blob/master/README.md#track-relinking)
     *
     * @return List of possibly-null full [Track] objects.
     */
    fun getTracks(vararg trackIds: String, market: Market? = null): SpotifyRestAction<List<Track?>> {
        return toAction(Supplier {
            get("https://api.spotify.com/v1/tracks?ids=${trackIds.map { it.encode() }.stream().collect(Collectors.joining(","))}${if (market != null) "&market=${market.code}" else ""}")
                    .toObject<TrackList>(api).tracks
        })
    }

    /**
     * Get a detailed audio analysis for a single track identified by its unique Spotify ID.
     *
     * @param trackId The Spotify ID for the track.
     *
     * @throws BadRequestException if [trackId] cannot be found
     */
    fun getAudioAnalysis(trackId: String): SpotifyRestAction<AudioAnalysis> {
        return toAction(Supplier {
            get("https://api.spotify.com/v1/audio-analysis/${trackId.encode()}").toObject<AudioAnalysis>(api)
        })
    }

    /**
     * Get audio feature information for a single track identified by its unique Spotify ID.
     *
     * @param trackId The Spotify ID for the track.
     *
     * @throws BadRequestException if [trackId] cannot be found
     */
    fun getAudioFeatures(trackId: String): SpotifyRestAction<AudioFeatures> {
        return toAction(Supplier {
            get("https://api.spotify.com/v1/audio-features/${trackId.encode()}").toObject<AudioFeatures>(api)
        })
    }

    /**
     * Get audio features for multiple tracks based on their Spotify IDs.
     *
     * @param trackId The Spotify ID for the track.
     *
     * @return Ordered list of possibly-null [AudioFeatures] objects.
     */
    fun getAudioFeatures(vararg trackIds: String): SpotifyRestAction<List<AudioFeatures?>> {
        return toAction(Supplier {
            get("https://api.spotify.com/v1/audio-features?ids=${trackIds.map { it.encode() }.stream().collect(Collectors.joining(","))}")
                    .toObject<AudioFeaturesResponse>(api).audio_features
        })
    }

}