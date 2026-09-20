#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
POLICY = ROOT / "app/src/main/java/com/goreecloud/dialer/ui/GlazeDialerPresentationPolicy.kt"
THEME = ROOT / "app/src/main/java/com/goreecloud/dialer/ui/GlazeDialerTheme.kt"
APP = ROOT / "app/src/main/java/com/goreecloud/dialer/ui/DialerApp.kt"
TEST = ROOT / "app/src/test/java/com/goreecloud/dialer/ui/GlazeDialerPresentationPolicyTest.kt"
PLATFORM = ROOT / "goreecloud.platform.yaml"
README = ROOT / "README.md"
SPECIFICATIONS = ROOT / "SPECIFICATIONS.md"
NOTES = ROOT / "NOTES.md"

VERSION = "1.6.0"
SOURCE_REVISION = "a7180679ea851389e0f3004515f9a25f420e716d"


def fail(message: str) -> None:
    raise SystemExit(f"Dialer GLAZE UI V1.6 source boundary failed: {message}")


def read(path: Path, label: str) -> str:
    if not path.is_file():
        fail(f"missing {label}: {path.relative_to(ROOT)}")
    return path.read_text(encoding="utf-8")


def require(text: str, marker: str, label: str) -> None:
    if marker not in text:
        fail(f"{label} missing {marker!r}")


def main() -> None:
    policy = read(POLICY, "presentation policy")
    theme = read(THEME, "theme boundary")
    app = read(APP, "Dialer app")
    test = read(TEST, "presentation policy tests")
    platform = read(PLATFORM, "Platform Contract")
    readme = read(README, "README")
    specifications = read(SPECIFICATIONS, "specifications")
    notes = read(NOTES, "notes")

    for marker in (
        f'const val StableVersion = "{VERSION}"',
        f'const val StableSourceRevision = "{SOURCE_REVISION}"',
        "const val InheritedCoarseInteractionFloorDp = 44",
        "const val InheritedPointerCompactFloorDp = 32",
        "const val InteractionFloorDp = 48",
        "const val TouchAssistanceFloorDp = 56",
        "const val TelephonyAuthorityMayBeDerivedFromPresentation = false",
        "const val CarrierAcceptanceMayBeDerivedFromPresentation = false",
        "const val EmergencyAuthorityMayBeDerivedFromPresentation = false",
        "context.reducedTransparency",
        "context.reducedMotion",
        "context.largeText || context.extraLargeText",
        "context.keyboardFirst",
    ):
        require(policy, marker, "presentation policy")

    for marker in (
        "fun GlazeDialerTheme(",
        "LocalGlazeDialerPresentationContext",
        "MaterialTheme(",
        "certainty-first",
    ):
        require(theme, marker, "theme boundary")

    for marker in (
        "GlazeDialerTheme {",
        "GlazeDialerPresentationPolicy.resolve(",
        "requestedMaterial = GlazeDialerMaterialRole.SOLID",
        "minimumInteractionTargetDp = presentation.minimumInteractionTargetDp",
        "Carrier call placement is not active in this Development build.",
    ):
        require(app, marker, "Dialer app")

    for marker in (
        "exactStableV16AuthorityIsPinned",
        "reducedTransparencyFailsGlassDownToSolid",
        "touchAssistanceKeepsLargerTargetWithoutCreatingTelephonyAuthority",
    ):
        require(test, marker, "presentation policy tests")

    for marker in (
        '  glaze_ui:\n    result: applicable-blocked\n    version: "1.6.0"',
        "GlazeDialerPresentationPolicy.kt",
        "GlazeDialerTheme.kt",
        "GlazeDialerPresentationPolicyTest.kt",
        "scripts/check_glaze_ui_v16.py",
        '  glaze_ui_required: "1.6.0"',
        "glaze-ui==1.6.0",
        "conformance:\n  status: nonconformant",
        "Physical-device and carrier telephony acceptance in issue #14 remains incomplete",
    ):
        require(platform, marker, "Platform Contract")

    for content, label in (
        (readme, "README"),
        (specifications, "specifications"),
        (notes, "notes"),
    ):
        require(content, "GLAZE UI V1.6", label)
        require(content, SOURCE_REVISION, label)
        require(content, "issue #14", label)

    stale_platform_markers = (
        "result: applicable-migration-required",
        "current Android Development UI requires repository-local reconciliation",
    )
    for marker in stale_platform_markers:
        if marker in platform:
            fail(f"Platform Contract retains stale migration marker {marker!r}")

    print(
        "Dialer GLAZE UI V1.6 source boundary passed: exact Stable provenance, "
        "certainty-first presentation policy, fail-closed telephony authority, and "
        "application-acceptance blockers are synchronized."
    )


if __name__ == "__main__":
    main()
