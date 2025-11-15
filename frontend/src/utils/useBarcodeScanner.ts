import { useEffect, useRef, useState } from 'react';
import { BrowserMultiFormatReader } from '@zxing/browser';
import { BARCODE_HINTS, normalizeRetailCode } from '../utils/barcodeUtils';
import { getRearCamera, stopCamera } from '../utils/cameraUtils';

type ScanStatus = "idle" | "scanning" | "processing" | "done";

export function useBarcodeScanner(
    onScan: (ean: string) => Promise<void>,
    enabled: boolean = true
) {
    const videoRef = useRef<HTMLVideoElement | null>(null);
    const [status, setStatus] = useState<ScanStatus>("idle");
    const [error, setError] = useState<string | null>(null);
    const [lastCode, setLastCode] = useState<string | null>(null);


    useEffect(() => {
        if (enabled) {
            setStatus("scanning");
        }
    }, [enabled]);

    useEffect(() => {
        if (status !== "scanning") return;

        const codeReader = new BrowserMultiFormatReader(BARCODE_HINTS);
        let stop: (() => void) | null = null;
        const el = videoRef.current;
        if(!el) return;


        (async () => {
            try {
                setError(null);

                const rear = await getRearCamera();
                if (!rear) {
                    setError("No camera found.");
                    return;
                }

                const constraints: MediaStreamConstraints = {
                    video: { deviceId: { exact: rear.deviceId}},
                    audio:false
                };

                const ctrls = await codeReader.decodeFromConstraints(
                    constraints,
                    el,
                    async(result, _err, controls) => {
                        if(!result) return;

                        const fmt =
                            (result as any).getBarcodeFormat?.() ?? (result as any).format;
                        const normalized = normalizeRetailCode(result.getText(), fmt);
                        if (!normalized || normalized === lastCode) return;

                        setLastCode(normalized);

                        controls.stop();
                        stopCamera(el);
                        setStatus("processing");

                        try {
                            await onScan(normalized);
                            setStatus("done");
                        } catch (e: any) {
                            console.error(e);
                            setError(e?.message ?? "Something went wrong");
                            setStatus("done");
                        }
                    }
                );
                stop = () => ctrls.stop();
            } catch (e: any) {
                setError("No camera found.");
            }
        })();
        return () => {
            if (stop) stop();
            stopCamera(el)
        };
    }, [status, lastCode, onScan]);

    const reset = () => {
        setError(null);
        setLastCode(null);
        setStatus("scanning");
    }

    return {
        videoRef,
        status,
        error,
        reset
    }
}
