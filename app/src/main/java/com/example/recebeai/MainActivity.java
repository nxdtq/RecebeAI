
package com.example.recebeai;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText editEmail;
    private EditText editPassword;
    private Button buttonLogin;
    private Button buttonRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());

        setContentView(R.layout.activity_main);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Localizar componentes da tela
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);

        // Configurar botão Entrar
        buttonLogin.setOnClickListener(v -> realizarLogin());
        buttonRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CadastroActivity.class);
            startActivity(intent);
        });

        // Ajustar interface às barras do Android
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom);

                    return insets;
                });
    }

    // Realizar autenticação e consultar perfil
    private void realizarLogin() {

        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,
                    "Preencha todos os campos!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        buttonLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, authTask -> {

                    if (!authTask.isSuccessful()) {
                        buttonLogin.setEnabled(true);

                        Toast.makeText(this,
                                "E-mail ou senha inválidos.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Recuperar UID do usuário
                    String uid = mAuth.getCurrentUser().getUid();

                    // Consultar perfil no Firestore
                    db.collection("users")
                            .document(uid)
                            .get()
                            .addOnCompleteListener(profileTask -> {

                                buttonLogin.setEnabled(true);

                                if (!profileTask.isSuccessful()) {
                                    mAuth.signOut();

                                    Toast.makeText(this,
                                            "Erro ao consultar seu perfil.",
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (!profileTask.getResult().exists()) {
                                    mAuth.signOut();

                                    Toast.makeText(this,
                                            "Perfil não encontrado.",
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }

                                // Recuperar dados do usuário
                                String role = profileTask.getResult()
                                        .getString("role");

                                Boolean active = profileTask.getResult()
                                        .getBoolean("active");

                                // Verificar se a conta está ativa
                                if (!Boolean.TRUE.equals(active)) {
                                    mAuth.signOut();

                                    Toast.makeText(this,
                                            "Sua conta está desativada.",
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }

                                // Abrir tela conforme o perfil
                                abrirTelaPorPerfil(role);
                            });
                });
    }

    // Redirecionar usuário para o seu perfil
    private void abrirTelaPorPerfil(String role) {

        Intent intent;

        if ("admin".equals(role)) {
            intent = new Intent(this, AdminActivity.class);

        } else if ("portaria".equals(role)) {
            intent = new Intent(this, PortariaActivity.class);

        } else if ("morador".equals(role)) {
            intent = new Intent(this, MoradorActivity.class);

        } else {
            mAuth.signOut();

            Toast.makeText(this,
                    "Perfil não reconhecido.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        startActivity(intent);

        // Impede voltar à tela de login pelo botão Voltar
        finish();
    }
}
