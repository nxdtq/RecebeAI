
package com.example.recebeai;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminActivity extends AppCompatActivity {

    private Button buttonManageUsers;
    private Button buttonManageUnits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());

        setContentView(R.layout.activity_admin);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                });

        buttonManageUsers = findViewById(R.id.buttonManageUsers);
        buttonManageUnits = findViewById(R.id.buttonManageUnits);

        buttonManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(
                    AdminActivity.this,
                    GerenciarUsuariosActivity.class
            );

            startActivity(intent);
        });

        buttonManageUnits.setOnClickListener(v -> {
            Intent intent = new Intent(
                    AdminActivity.this,
                    GerenciarUnidadesActivity.class
            );

            startActivity(intent);
        });
    }
}