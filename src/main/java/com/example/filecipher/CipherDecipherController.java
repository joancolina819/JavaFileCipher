package com.example.filecipher;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class CipherDecipherController {

    @FXML
    private TextField cipherFileAddress;

    @FXML
    private PasswordField cipherPass;

    @FXML
    private PasswordField cipherPassConfirm;

    @FXML
    private TextField decipherFileAddress;

    @FXML
    private PasswordField decipherPass;

    @FXML
    private PasswordField decipherPassConfirmed;

    @FXML
    void cipherBtnPressed(ActionEvent event) {
        if ((cipherPass.getText().equals(cipherPassConfirm.getText()) && !cipherPass.getText().equals(""))) {
            File toEncrypt = new File(cipherFileAddress.getText());
            encryptDecrypt(toEncrypt, cipherPass.getText(), Cipher.ENCRYPT_MODE);
        } else {
            showAlert();
        }
    }

    @FXML
    void decipherBtnPressed(ActionEvent event) {
        if ((decipherPass.getText().equals(decipherPassConfirmed.getText()) && !decipherPass.getText().equals(""))) {
            File toDecrypt = new File(decipherFileAddress.getText());
            encryptDecrypt(toDecrypt, decipherPass.getText(), Cipher.DECRYPT_MODE);
        } else {
            showAlert();
        }
    }

    @FXML
    void findCipherBtnPressed(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escoja el archivo a cifrar");
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        cipherFileAddress.setText(file.getAbsolutePath());
    }

    @FXML
    void findDecipherBtnPressed(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escoja el archivo a descifrar");
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        decipherFileAddress.setText(file.getAbsolutePath());
    }

    private void showAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error");
        alert.showAndWait();
        cleanFields();
    }

    private void cleanFields() {

        // Cifrar
        cipherFileAddress.setText("");
        cipherPass.setText("");
        cipherPassConfirm.setText("");

        // Descrifrar
        decipherFileAddress.setText("");
        decipherPass.setText("");
        decipherPassConfirmed.setText("");
    }

    private void encryptDecrypt(File toEncrypt, String password, int type) {
        try {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(toEncrypt));
            BufferedOutputStream out;
            File toOut = null;
            if (type == Cipher.ENCRYPT_MODE){
                toOut = new File("encrypted.txt");
                out = new BufferedOutputStream(new FileOutputStream(toOut));
            }else{
                toOut = new File("decrypted.txt");
                out = new BufferedOutputStream(new FileOutputStream(toOut));
            }
            SecretKeyFactory kf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] salt = new byte[20];
            byte[] iv = new byte[16];
            KeySpec ks = new PBEKeySpec(password.toCharArray(), salt, 1000, 128);
            SecretKey key = kf.generateSecret(ks);
            byte[] aesKey = key.getEncoded();
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            cipher.init(type, keySpec, ivspec);
            byte[] inputBuffer = new byte[1];
            int bytesRead = in.read(inputBuffer);
            while (bytesRead > 0) {
                out.write(cipher.update(inputBuffer, 0, bytesRead));
                bytesRead = in.read(inputBuffer);
            }
            out.write(cipher.doFinal());
            in.close();
            Runtime.getRuntime().exec("explorer.exe /select," + toOut.getAbsolutePath());
            out.close();
            cleanFields();
        }catch (FileNotFoundException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error");
            alert.setContentText("No se encontró el archivo.");
            alert.showAndWait();
            cleanFields();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException |
                InvalidKeyException | IOException | BadPaddingException | IllegalBlockSizeException |
                InvalidKeySpecException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error");
            alert.setContentText("Error al cifrar.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            cleanFields();
        }

    }

}
