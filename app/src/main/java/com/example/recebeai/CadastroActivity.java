package com.example.recebeai;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CadastroActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText editName;
    private EditText editEmail;
    private EditText editPassword;

    private Button buttonRegister;
    private Button buttonBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);

        buttonRegister = findViewById(R.id.buttonRegister);
        buttonBackToLogin = findViewById(R.id.buttonBackToLogin);

        // Botão para cadastrar
        buttonRegister.setOnClickListener(v -> realizarCadastro());

        // Botão para voltar ao login
        buttonBackToLogin.setOnClickListener(v -> finish());
    }

    private void realizarCadastro() {

        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString();

        // Verifica se os campos foram preenchidos
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,
                    "Preencha todos os campos.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this,
                    "A senha deve ter pelo menos 6 caracteres.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        buttonRegister.setEnabled(false);

        // Cria a conta no Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        if (firebaseUser == null) {
                            buttonRegister.setEnabled(true);
                            Toast.makeText(this,
                                    "Não foi possível concluir o cadastro.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        Map<String, Object> userData = new HashMap<>();

                        userData.put("name", name);
                        userData.put("email", firebaseUser.getEmail());
                        userData.put("role", "morador");
                        userData.put("active", false);
                        userData.put("createdAt", FieldValue.serverTimestamp());

                        db.collection("users")
                                .document(firebaseUser.getUid())
                                .set(userData)
                                .addOnSuccessListener(unused -> {
                                    mAuth.signOut();

                                    Toast.makeText(this,
                                            "Cadastro realizado! Aguarde a aprovação da administração.",
                                            Toast.LENGTH_LONG).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {

                                    // Tenta excluir a conta criada no Authentication para n ter perfil nulo
                                    firebaseUser.delete()
                                            .addOnCompleteListener(deleteTask -> {

                                                mAuth.signOut();
                                                buttonRegister.setEnabled(true);

                                                Toast.makeText(this,
                                                        "Não foi possível salvar seu cadastro. Tente novamente.",
                                                        Toast.LENGTH_LONG).show();
                                            });
                                });

                    } else {

                        // exibe mensagem de erro caso de errado o authentication
                        buttonRegister.setEnabled(true);

                        String mensagem = task.getException() != null
                                ? task.getException().getMessage()
                                : "Não foi possível realizar o cadastro.";

                        Toast.makeText(this,
                                mensagem,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}