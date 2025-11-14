
export async function getRearCamera() {
    const tmp = await navigator.mediaDevices.getUserMedia({
        video: true,
        audio: false,
    });
    tmp.getTracks().forEach(t => t.stop());

    const devices = await navigator.mediaDevices.enumerateDevices();
    const videoInputs = devices.filter(d => d.kind === "videoinput");

    const rear =
        videoInputs.find(d => /back|rear|environment|tras|trasera|wel|umgebung|back.*camera/i.test(d.label)) ||
        videoInputs.find(d => !/front|user|facetime|truedepth/i.test(d.label)) ||
        videoInputs[videoInputs.length - 1];

    return rear || null;
}

export function stopCamera(videoElement: HTMLVideoElement | null) {
    const stream = videoElement?.srcObject as MediaStream | null;
    stream?.getTracks().forEach(t => t.stop());
    if (videoElement) videoElement.srcObject = null;
}