package com.goreecloud.dialer.telephony

import android.os.Build
import android.os.OutcomeReceiver
import android.os.ParcelUuid
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.CallEndpoint
import android.telecom.CallEndpointException
import android.telecom.InCallService
import java.util.IdentityHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Development InCallService lifecycle, presentation, and audio-control boundary.
 *
 * The service tracks only process-local call sessions and content-minimized lifecycle/audio/control,
 * conference, and post-dial wait state. Development incoming/ongoing presentation exists, but
 * production UI replacement and ringtone ownership remain undeclared until end-to-end device
 * validation is complete.
 */
class GoreeCloudInCallService : InCallService() {
    private val callbacks = IdentityHashMap<Call, Call.Callback>()
    private val sessionIds = IdentityHashMap<Call, Long>()
    private val incomingCallPresenter by lazy { AndroidIncomingCallPresenter(this) }
    private val nextRouteId = AtomicLong(1)
    private val routeIdsByIdentifier = linkedMapOf<ParcelUuid, Long>()
    private val endpointsByRouteId = linkedMapOf<Long, CallEndpoint>()

    private val audioControlTarget = object : CallAudioControlTarget {
        override fun setMuted(isMuted: Boolean) {
            this@GoreeCloudInCallService.setMuted(isMuted)
        }
    }

    private val endpointRoutingTarget = object : CallEndpointRoutingTarget {
        override fun request(routeId: Long): CallEndpointRoutingResult {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                return CallEndpointRoutingResult.Rejected(
                    "CallEndpoint routing requires Android 14 or later",
                )
            }
            return requestEndpointApi34(routeId)
        }
    }

    override fun onCreate() {
        super.onCreate()
        InCallAudioControlRuntime.attach(audioControlTarget)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            InCallEndpointRoutingRuntime.attach(endpointRoutingTarget)
            InCallRuntimeStore.onAvailableEndpointsChanged(
                supported = true,
                endpoints = emptyList(),
            )
        }
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)

        val callback = object : Call.Callback() {
            override fun onStateChanged(call: Call, state: Int) {
                InCallRuntimeStore.onCallStateChanged(call, state)
                sessionIds[call]?.let { sessionId ->
                    incomingCallPresenter.sync(
                        sessionId = sessionId,
                        state = CallLifecycleStateMapper.fromAndroid(state),
                    )
                }
            }

            override fun onDetailsChanged(call: Call, details: Call.Details) {
                InCallRuntimeStore.onCallDetailsChanged(call, details)
            }

            override fun onConferenceableCallsChanged(
                call: Call,
                conferenceableCalls: List<Call>,
            ) {
                InCallRuntimeStore.onConferenceableCallsChanged(call, conferenceableCalls)
            }

            override fun onParentChanged(call: Call, parent: Call?) {
                InCallRuntimeStore.onParentChanged(call, parent)
            }

            override fun onChildrenChanged(call: Call, children: List<Call>) {
                InCallRuntimeStore.onChildrenChanged(call, children)
            }

            override fun onPostDialWait(call: Call, remainingPostDialSequence: String) {
                InCallRuntimeStore.onPostDialWait(call, remainingPostDialSequence)
            }
        }

        callbacks[call] = callback
        call.registerCallback(callback)
        val sessionId = InCallRuntimeStore.onCallAdded(call)
        sessionIds[call] = sessionId
        incomingCallPresenter.sync(
            sessionId = sessionId,
            state = CallLifecycleStateMapper.fromAndroid(call.state),
        )
    }

    override fun onCallRemoved(call: Call) {
        sessionIds.remove(call)?.let(incomingCallPresenter::cancel)
        callbacks.remove(call)?.let(call::unregisterCallback)
        InCallRuntimeStore.onCallRemoved(call)
        super.onCallRemoved(call)
    }

    override fun onCanAddCallChanged(canAddCall: Boolean) {
        super.onCanAddCallChanged(canAddCall)
        InCallRuntimeStore.onCanAddCallChanged(canAddCall)
    }

    @Suppress("DEPRECATION")
    override fun onCallAudioStateChanged(audioState: CallAudioState) {
        super.onCallAudioStateChanged(audioState)
        InCallRuntimeStore.onMuteStateChanged(audioState.isMuted)
    }

    override fun onMuteStateChanged(isMuted: Boolean) {
        InCallRuntimeStore.onMuteStateChanged(isMuted)
    }

    override fun onCallEndpointChanged(callEndpoint: CallEndpoint) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
        val routeId = routeIdForApi34(callEndpoint)
        endpointsByRouteId[routeId] = callEndpoint
        InCallRuntimeStore.onCurrentEndpointChanged(routeId)
    }

    override fun onAvailableCallEndpointsChanged(availableEndpoints: List<CallEndpoint>) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
        endpointsByRouteId.clear()
        val summaries = availableEndpoints.map { endpoint ->
            val routeId = routeIdForApi34(endpoint)
            endpointsByRouteId[routeId] = endpoint
            CallEndpointRuntimeSummary(
                routeId = routeId,
                kind = endpointKindApi34(endpoint.endpointType),
            )
        }
        InCallRuntimeStore.onAvailableEndpointsChanged(
            supported = true,
            endpoints = summaries,
        )
    }

    override fun onDestroy() {
        InCallAudioControlRuntime.detach(audioControlTarget)
        InCallEndpointRoutingRuntime.detach(endpointRoutingTarget)
        endpointsByRouteId.clear()
        routeIdsByIdentifier.clear()
        sessionIds.values.forEach(incomingCallPresenter::cancel)
        sessionIds.clear()
        callbacks.forEach { (call, callback) -> call.unregisterCallback(callback) }
        callbacks.clear()
        InCallRuntimeStore.clear()
        super.onDestroy()
    }

    private fun routeIdForApi34(endpoint: CallEndpoint): Long =
        routeIdsByIdentifier.getOrPut(endpoint.identifier) { nextRouteId.getAndIncrement() }

    private fun endpointKindApi34(type: Int): CallEndpointKind = when (type) {
        CallEndpoint.TYPE_EARPIECE -> CallEndpointKind.EARPIECE
        CallEndpoint.TYPE_BLUETOOTH -> CallEndpointKind.BLUETOOTH
        CallEndpoint.TYPE_WIRED_HEADSET -> CallEndpointKind.WIRED_HEADSET
        CallEndpoint.TYPE_SPEAKER -> CallEndpointKind.SPEAKER
        CallEndpoint.TYPE_STREAMING -> CallEndpointKind.STREAMING
        else -> CallEndpointKind.UNKNOWN
    }

    private fun requestEndpointApi34(routeId: Long): CallEndpointRoutingResult {
        val endpoint = endpointsByRouteId[routeId]
            ?: return CallEndpointRoutingResult.Rejected(
                "Requested call endpoint is no longer available",
            )
        InCallRuntimeStore.onEndpointRequestChanged(
            CallEndpointRequestEvidence(
                routeId = routeId,
                state = CallEndpointRequestState.SUBMITTED,
            ),
        )

        return try {
            requestCallEndpointChange(
                endpoint,
                mainExecutor,
                object : OutcomeReceiver<Void?, CallEndpointException> {
                    override fun onResult(result: Void?) {
                        InCallRuntimeStore.onEndpointRequestChanged(
                            CallEndpointRequestEvidence(
                                routeId = routeId,
                                state = CallEndpointRequestState.SUCCEEDED,
                            ),
                        )
                    }

                    override fun onError(error: CallEndpointException) {
                        InCallRuntimeStore.onEndpointRequestChanged(
                            CallEndpointRequestEvidence(
                                routeId = routeId,
                                state = CallEndpointRequestState.FAILED,
                                reason = error.message?.takeIf { it.isNotBlank() }
                                    ?: "Android rejected the endpoint change",
                            ),
                        )
                    }
                },
            )
            CallEndpointRoutingResult.Submitted
        } catch (securityException: SecurityException) {
            CallEndpointRoutingResult.Failed("Android rejected call endpoint authorization")
        } catch (runtimeException: RuntimeException) {
            CallEndpointRoutingResult.Failed(
                runtimeException.message?.takeIf { it.isNotBlank() }
                    ?: runtimeException::class.java.simpleName,
            )
        }
    }
}
