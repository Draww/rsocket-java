/*
 * Copyright 2015-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rsocket.test;

/** Test instances of all frame types. */
public final class TestFrames {
//
//  private TestFrames() {}
//
//  /**
//   * Returns a test instance of {@link CancelFrame}.
//   *
//   * @return a test instance of {@link CancelFrame}
//   */
//  public static CancelFrame createTestCancelFrame() {
//    return createCancelFrame(DEFAULT);
//  }
//
//  /**
//   * Returns a test instance of {@link ErrorFrame}.
//   *
//   * @return a test instance of {@link ErrorFrame}
//   */
//  public static ErrorFrame createTestErrorFrame() {
//    return createErrorFrame(DEFAULT, 1, (ByteBuf) null);
//  }
//
//  /**
//   * Returns a test instance of {@link ExtensionFrameFlyweight}.
//   *
//   * @return a test instance of {@link ExtensionFrameFlyweight}
//   */
//  public static ExtensionFrameFlyweight createTestExtensionFrame() {
//    return createExtensionFrame(DEFAULT, true, 1, (ByteBuf) null, null);
//  }
//
//  /**
//   * Returns a custom test {@link Frame}.
//   *
//   * @param frameType the type of frame
//   * @param byteBuf the {@link ByteBuf} of content for this frame
//   * @return a custom test {@link Frame}
//   */
//  public static Frame createTestFrame(FrameType frameType, ByteBuf byteBuf) {
//    return new TestFrame(frameType, byteBuf);
//  }
//
//  /**
//   * Returns a test instance of {@link FrameLengthFrame}.
//   *
//   * @return a test instance of {@link FrameLengthFrame}
//   */
//  public static FrameLengthFrame createTestFrameLengthFrame() {
//    return createFrameLengthFrame(DEFAULT, createTestStreamIdFrame());
//  }
//
//  /**
//   * Returns a test instance of {@link KeepaliveFrame}.
//   *
//   * @return a test instance of {@link KeepaliveFrame}
//   */
//  public static KeepaliveFrame createTestKeepaliveFrame() {
//    return createKeepaliveFrame(DEFAULT, false, 1, null);
//  }
//
//  /**
//   * Returns a test instance of {@link LeaseFrame}.
//   *
//   * @return a test instance of {@link LeaseFrame}
//   */
//  public static LeaseFrame createTestLeaseFrame() {
//    return createLeaseFrame(DEFAULT, Duration.ofMillis(1), 1, null);
//  }
//
//  /**
//   * Returns a test instance of {@link MetadataPushFrame}.
//   *
//   * @return a test instance of {@link MetadataPushFrame}
//   */
//  public static MetadataPushFrame createTestMetadataPushFrame() {
//    return createMetadataPushFrame(DEFAULT, EMPTY_BUFFER);
//  }
//
//  /**
//   * Returns a test instance of {@link PayloadFrame}.
//   *
//   * @return a test instance of {@link PayloadFrame}
//   */
//  public static PayloadFrame createTestPayloadFrame() {
//    return createPayloadFrame(DEFAULT, false, true, (ByteBuf) null, null);
//  }
//
//  /**
//   * Returns a test instance of {@link RequestChannelFrame}.
//   *
//   * @return a test instance of {@link RequestChannelFrame}
//   */
//  public static RequestChannelFrame createTestRequestChannelFrame() {
//    return createRequestChannelFrame(DEFAULT, false, false, 1, (ByteBuf) null, null);
//  }
//
//  /**
//   * Returns a test instance of {@link RequestFireAndForgetFrame}.
//   *
//   * @return a test instance of {@link RequestFireAndForgetFrame}
//   */
//  public static RequestFireAndForgetFrame createTestRequestFireAndForgetFrame() {
//    return createRequestFireAndForgetFrame(DEFAULT, false, (ByteBuf) null, null);
//  }
//
//  /**
//   * Returns a test instance of {@link RequestNFrame}.
//   *
//   * @return a test instance of {@link RequestNFrame}
//   */
//  public static RequestNFrame createTestRequestNFrame() {
//    return createRequestNFrame(DEFAULT, 1);
//  }
//
//  /**
//   * Returns a test instance of {@link RequestResponseFrame}.
//   *
//   * @return a test instance of {@link RequestResponseFrame}
//   */
//  public static RequestResponseFrame createTestRequestResponseFrame() {
//    return createRequestResponseFrame(DEFAULT, false, (ByteBuf) null, null);
//  }
//
//  /**
//   * Returns a test instance of {@link RequestStreamFrame}.
//   *
//   * @return a test instance of {@link RequestStreamFrame}
//   */
//  public static RequestStreamFrame createTestRequestStreamFrame() {
//    return createRequestStreamFrame(DEFAULT, false, 1, (ByteBuf) null, null);
//  }
//
//  /**
//   * Returns a test instance of {@link ResumeFrame}.
//   *
//   * @return a test instance of {@link ResumeFrame}
//   */
//  public static ResumeFrame createTestResumeFrame() {
//    return createResumeFrame(DEFAULT, 1, 0, EMPTY_BUFFER, 1, 1);
//  }
//
//  /**
//   * Returns a test instance of {@link ResumeOkFrame}.
//   *
//   * @return a test instance of {@link ResumeOkFrame}
//   */
//  public static ResumeOkFrame createTestResumeOkFrame() {
//    return createResumeOkFrame(DEFAULT, 1);
//  }
//
//  /**
//   * Returns a test instance of {@link SetupFrame}.
//   *
//   * @return a test instance of {@link SetupFrame}
//   */
//  public static SetupFrame createTestSetupFrame() {
//    return createSetupFrame(
//        DEFAULT, true, 1, 1, Duration.ofMillis(1), Duration.ofMillis(1), null, "", "", null, null);
//  }
//
//  /**
//   * Returns a test instance of {@link StreamIdFrame}.
//   *
//   * @return a test instance of {@link StreamIdFrame}
//   */
//  public static StreamIdFrame createTestStreamIdFrame() {
//    return createStreamIdFrame(DEFAULT, 1, createTestCancelFrame());
//  }
//
//  private static final class TestFrame implements Frame {
//
//    private final ByteBuf byteBuf;
//
//    private final FrameType frameType;
//
//    private TestFrame(FrameType frameType, ByteBuf byteBuf) {
//      this.frameType = frameType;
//      this.byteBuf = byteBuf;
//    }
//
//    @Override
//    public void dispose() {}
//
//    @Override
//    public FrameType getFrameType() {
//      return frameType;
//    }
//
//    @Override
//    public ByteBuf getUnsafeFrame() {
//      return byteBuf.asReadOnly();
//    }
//  }
}
