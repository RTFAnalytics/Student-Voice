package ru.urfu.sv.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Component
@Slf4j
public class QRCodeService {
    private static final QRCodeWriter qrCodeWriter = new QRCodeWriter();

    public String getEncodedCode(String textContent, Integer width, Integer height) {
        try (ByteArrayOutputStream bao = new ByteArrayOutputStream()) {
            BitMatrix bitMatrix = qrCodeWriter.encode(textContent, BarcodeFormat.QR_CODE, width, height, Map.of(EncodeHintType.MARGIN, 0));
            ImageIO.write(MatrixToImageWriter.toBufferedImage(bitMatrix), "jpg", bao);
            return Base64.getEncoder().encodeToString(bao.toByteArray());
        } catch (IOException | WriterException e) {
            log.error("Произошла ошибка во время создания QR кода - ", e);
            return "";
        }
    }
}
