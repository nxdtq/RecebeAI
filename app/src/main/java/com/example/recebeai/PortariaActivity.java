package com.example.recebeai;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.Timestamp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PortariaActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private Spinner spinnerBlock;
    private Spinner spinnerApartment;
    private TextView textResident;
    private EditText editDescription;
    private Button buttonRegisterParcel;
    private Button buttonRefreshParcels;
    private TextView textPendingEmpty;
    private LinearLayout pendingParcelsContainer;

    private Button buttonRefreshHistory;
    private TextView textHistoryEmpty;
    private LinearLayout historyParcelsContainer;

    private ArrayList<UnitOption> blocksList;
    private ArrayList<UnitOption> apartmentsList;

    private ArrayAdapter<UnitOption> blocksAdapter;
    private ArrayAdapter<UnitOption> apartmentsAdapter;

    private String selectedResidentNames = "";

    private final ExecutorService signatureExecutor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_portaria);

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

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        spinnerBlock = findViewById(R.id.spinnerBlock);
        spinnerApartment = findViewById(R.id.spinnerApartment);
        textResident = findViewById(R.id.textResident);
        editDescription = findViewById(R.id.editDescription);
        buttonRegisterParcel = findViewById(R.id.buttonRegisterParcel);
        buttonRefreshParcels = findViewById(R.id.buttonRefreshParcels);
        textPendingEmpty = findViewById(R.id.textPendingEmpty);
        pendingParcelsContainer = findViewById(R.id.pendingParcelsContainer);

        buttonRefreshHistory = findViewById(R.id.buttonRefreshHistory);
        textHistoryEmpty = findViewById(R.id.textHistoryEmpty);
        historyParcelsContainer = findViewById(R.id.historyParcelsContainer);

        blocksList = new ArrayList<>();
        apartmentsList = new ArrayList<>();

        configurarListas();
        carregarBlocos();
        carregarEncomendasPendentes();
        carregarHistoricoEncomendas();

        buttonRegisterParcel.setOnClickListener(v -> registrarEncomenda());
        buttonRefreshParcels.setOnClickListener(v -> carregarEncomendasPendentes());
        buttonRefreshHistory.setOnClickListener(v -> carregarHistoricoEncomendas());
    }

    @Override
    protected void onDestroy() {
        signatureExecutor.shutdown();
        super.onDestroy();
    }

    private void configurarListas() {

        blocksAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                blocksList
        );

        blocksAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        spinnerBlock.setAdapter(blocksAdapter);

        apartmentsAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                apartmentsList
        );

        apartmentsAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        spinnerApartment.setAdapter(apartmentsAdapter);

        spinnerBlock.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        if (position < 0 || position >= blocksList.size()) {
                            return;
                        }

                        UnitOption selectedBlock = blocksList.get(position);
                        carregarApartamentos(selectedBlock.id);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        spinnerApartment.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        if (position < 0 || position >= apartmentsList.size()) {
                            return;
                        }

                        UnitOption selectedApartment = apartmentsList.get(position);

                        if (selectedApartment.id.isEmpty()) {
                            selectedResidentNames = "";
                            textResident.setText("Selecione um apartamento");
                            return;
                        }

                        carregarMoradores(selectedApartment.id);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        selectedResidentNames = "";
                        textResident.setText("Selecione um apartamento");
                    }
                });
    }

    private void carregarBlocos() {

        blocksList.clear();
        blocksList.add(new UnitOption("", "Carregando blocos..."));
        blocksAdapter.notifyDataSetChanged();

        db.collection("blocks")
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    blocksList.clear();
                    blocksList.add(new UnitOption("", "Selecione um bloco"));

                    for (QueryDocumentSnapshot document : querySnapshot) {

                        String name = document.getString("name");

                        blocksList.add(new UnitOption(
                                document.getId(),
                                name
                        ));
                    }

                    blocksAdapter.notifyDataSetChanged();

                    if (blocksList.size() == 1) {
                        Toast.makeText(
                                this,
                                "Nenhum bloco ativo encontrado.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                })
                .addOnFailureListener(e -> {

                    blocksList.clear();
                    blocksList.add(new UnitOption("", "Erro ao carregar blocos"));
                    blocksAdapter.notifyDataSetChanged();

                    Toast.makeText(
                            this,
                            "Erro ao carregar blocos: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void carregarApartamentos(String blockId) {

        apartmentsList.clear();
        apartmentsList.add(new UnitOption("", "Selecione um apartamento"));
        apartmentsAdapter.notifyDataSetChanged();

        selectedResidentNames = "";
        textResident.setText("Selecione um apartamento");

        if (blockId == null || blockId.isEmpty()) {
            return;
        }

        apartmentsList.clear();
        apartmentsList.add(new UnitOption("", "Carregando apartamentos..."));
        apartmentsAdapter.notifyDataSetChanged();

        db.collection("apartments")
                .whereEqualTo("blockId", blockId)
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    apartmentsList.clear();
                    apartmentsList.add(new UnitOption("", "Selecione um apartamento"));

                    for (QueryDocumentSnapshot document : querySnapshot) {

                        String number = document.getString("number");

                        apartmentsList.add(new UnitOption(
                                document.getId(),
                                number
                        ));
                    }

                    apartmentsAdapter.notifyDataSetChanged();

                    if (apartmentsList.size() == 1) {
                        Toast.makeText(
                                this,
                                "Nenhum apartamento ativo encontrado neste bloco.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                })
                .addOnFailureListener(e -> {

                    apartmentsList.clear();
                    apartmentsList.add(new UnitOption("", "Erro ao carregar apartamentos"));
                    apartmentsAdapter.notifyDataSetChanged();

                    Toast.makeText(
                            this,
                            "Erro ao carregar apartamentos: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void carregarMoradores(String apartmentId) {

        selectedResidentNames = "";
        textResident.setText("Buscando morador...");

        db.collection("residentDirectory")
                .whereEqualTo("apartmentId", apartmentId)
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {
                        textResident.setText(
                                "Nenhum morador ativo vinculado a este apartamento."
                        );
                        return;
                    }

                    StringBuilder nomes = new StringBuilder();

                    for (QueryDocumentSnapshot document : querySnapshot) {

                        String nome = document.getString("name");

                        if (nome != null && !nome.trim().isEmpty()) {

                            if (nomes.length() > 0) {
                                nomes.append("\n");
                            }

                            nomes.append("• ").append(nome);
                        }
                    }

                    if (nomes.length() == 0) {
                        textResident.setText(
                                "Não foi possível identificar o nome do morador."
                        );
                    } else {
                        selectedResidentNames = nomes.toString();
                        textResident.setText("Morador(es):\n" + selectedResidentNames);
                    }
                })
                .addOnFailureListener(e -> {

                    selectedResidentNames = "";
                    textResident.setText(
                            "Não foi possível consultar o morador."
                    );

                    Toast.makeText(
                            this,
                            "Erro ao buscar morador: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void registrarEncomenda() {

        if (spinnerBlock.getSelectedItemPosition() <= 0 ||
                spinnerBlock.getSelectedItemPosition() >= blocksList.size()) {

            Toast.makeText(
                    this,
                    "Selecione um bloco.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (spinnerApartment.getSelectedItemPosition() <= 0 ||
                spinnerApartment.getSelectedItemPosition() >= apartmentsList.size()) {

            Toast.makeText(
                    this,
                    "Selecione um apartamento.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String description = editDescription.getText().toString().trim();

        if (description.isEmpty()) {
            editDescription.setError("Informe a descrição da encomenda.");
            editDescription.requestFocus();
            return;
        }

        if (selectedResidentNames.isEmpty()) {
            Toast.makeText(
                    this,
                    "Não há morador ativo identificado neste apartamento.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        if (auth.getCurrentUser() == null) {
            Toast.makeText(
                    this,
                    "Sua sessão expirou. Faça login novamente.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        UnitOption selectedBlock =
                blocksList.get(spinnerBlock.getSelectedItemPosition());

        UnitOption selectedApartment =
                apartmentsList.get(spinnerApartment.getSelectedItemPosition());

        String registeredBy = auth.getCurrentUser().getUid();

        Map<String, Object> parcel = new HashMap<>();
        parcel.put("blockId", selectedBlock.id);
        parcel.put("blockName", selectedBlock.name);
        parcel.put("apartmentId", selectedApartment.id);
        parcel.put("apartmentNumber", selectedApartment.name);
        parcel.put("residentNames", selectedResidentNames);
        parcel.put("description", description);
        parcel.put("status", "pending");
        parcel.put("registeredBy", registeredBy);
        parcel.put("createdAt", FieldValue.serverTimestamp());

        buttonRegisterParcel.setEnabled(false);

        db.collection("parcels")
                .add(parcel)
                .addOnSuccessListener(documentReference -> {

                    Toast.makeText(
                            this,
                            "Encomenda registrada com sucesso!",
                            Toast.LENGTH_LONG
                    ).show();

                    editDescription.setText("");
                    buttonRegisterParcel.setEnabled(true);
                    carregarEncomendasPendentes();
                })
                .addOnFailureListener(e -> {

                    buttonRegisterParcel.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Erro ao registrar encomenda: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void carregarEncomendasPendentes() {

        pendingParcelsContainer.removeAllViews();
        textPendingEmpty.setVisibility(View.VISIBLE);
        textPendingEmpty.setText("Carregando encomendas...");

        db.collection("parcels")
                .whereEqualTo("status", "pending")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    pendingParcelsContainer.removeAllViews();

                    if (querySnapshot.isEmpty()) {
                        textPendingEmpty.setText("Nenhuma encomenda pendente.");
                        textPendingEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    textPendingEmpty.setVisibility(View.GONE);

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        adicionarCardEncomenda(document);
                    }
                })
                .addOnFailureListener(e -> {

                    textPendingEmpty.setVisibility(View.VISIBLE);
                    textPendingEmpty.setText("Não foi possível carregar as encomendas.");

                    Toast.makeText(
                            this,
                            "Erro ao carregar encomendas: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void adicionarCardEncomenda(QueryDocumentSnapshot document) {

        LinearLayout card = criarCard();

        String blockName = document.getString("blockName");
        String apartmentNumber = document.getString("apartmentNumber");
        String residentNames = document.getString("residentNames");
        String description = document.getString("description");

        adicionarTextoAoCard(card, "Bloco: " + valorOuTraco(blockName), true);
        adicionarTextoAoCard(card, "Apartamento: " + valorOuTraco(apartmentNumber), false);
        adicionarTextoAoCard(card, "Morador(es):\n" + valorOuTraco(residentNames), false);
        adicionarTextoAoCard(card, "Descrição: " + valorOuTraco(description), false);
        adicionarTextoAoCard(card, "Status: Pendente", false);

        Timestamp timestamp = document.getTimestamp("createdAt");

        adicionarTextoAoCard(
                card,
                "Recebida em: " + formatarData(timestamp),
                false
        );

        Button buttonDeliver = new Button(this);
        buttonDeliver.setText("Registrar retirada");
        buttonDeliver.setOnClickListener(v ->
                solicitarDadosRetirada(document.getId()));

        card.addView(buttonDeliver);
        pendingParcelsContainer.addView(card);
    }

    private void carregarHistoricoEncomendas() {

        historyParcelsContainer.removeAllViews();
        textHistoryEmpty.setVisibility(View.VISIBLE);
        textHistoryEmpty.setText("Carregando histórico...");

        db.collection("parcels")
                .whereEqualTo("status", "delivered")
                .orderBy("deliveredAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    historyParcelsContainer.removeAllViews();

                    if (querySnapshot.isEmpty()) {
                        textHistoryEmpty.setText("Nenhuma encomenda retirada ainda.");
                        textHistoryEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    textHistoryEmpty.setVisibility(View.GONE);

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        adicionarCardHistorico(document);
                    }
                })
                .addOnFailureListener(e -> {

                    textHistoryEmpty.setVisibility(View.VISIBLE);
                    textHistoryEmpty.setText("Não foi possível carregar o histórico.");

                    Toast.makeText(
                            this,
                            "Erro ao carregar histórico: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void adicionarCardHistorico(QueryDocumentSnapshot document) {

        LinearLayout card = criarCard();

        adicionarTextoAoCard(
                card,
                "Bloco: " + valorOuTraco(document.getString("blockName")),
                true
        );

        adicionarTextoAoCard(
                card,
                "Apartamento: " + valorOuTraco(document.getString("apartmentNumber")),
                false
        );

        adicionarTextoAoCard(
                card,
                "Morador(es):\n" + valorOuTraco(document.getString("residentNames")),
                false
        );

        adicionarTextoAoCard(
                card,
                "Descrição: " + valorOuTraco(document.getString("description")),
                false
        );

        adicionarTextoAoCard(
                card,
                "Retirado por: " + valorOuTraco(document.getString("receiverName")),
                false
        );

        adicionarTextoAoCard(card, "Status: Retirada", false);

        Timestamp deliveredAt = document.getTimestamp("deliveredAt");

        adicionarTextoAoCard(
                card,
                "Retirada em: " + formatarData(deliveredAt),
                false
        );

        String signaturePath = document.getString("signaturePath");

        Button buttonSignature = new Button(this);
        buttonSignature.setText("Visualizar assinatura");

        buttonSignature.setOnClickListener(v ->
                visualizarAssinatura(signaturePath));

        if (signaturePath == null || signaturePath.trim().isEmpty()) {
            buttonSignature.setEnabled(false);
            buttonSignature.setText("Assinatura não disponível");
        }

        card.addView(buttonSignature);
        historyParcelsContainer.addView(card);
    }

    private void visualizarAssinatura(String signaturePath) {

        if (signaturePath == null || signaturePath.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Esta encomenda não possui assinatura registrada.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        File signatureFile = new File(getFilesDir(), signaturePath);

        if (!signatureFile.exists()) {
            Toast.makeText(
                    this,
                    "A assinatura não foi encontrada neste aparelho.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(signatureFile.getAbsolutePath());

        if (bitmap == null) {
            Toast.makeText(
                    this,
                    "Não foi possível abrir a assinatura.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setPadding(24, 24, 24, 24);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Assinatura de retirada")
                .setView(imageView)
                .setPositiveButton("Fechar", (dialogInterface, which) -> {
                    imageView.setImageDrawable(null);
                    bitmap.recycle();
                })
                .create();

        dialog.setOnDismissListener(dialogInterface -> {
            imageView.setImageDrawable(null);

            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        });

        dialog.show();
    }

    private void solicitarDadosRetirada(String parcelId) {

        EditText inputName = new EditText(this);
        inputName.setHint("Nome de quem retirou");
        inputName.setSingleLine(true);
        inputName.setPadding(40, 24, 40, 24);

        TextView signatureLabel = new TextView(this);
        signatureLabel.setText("Assinatura de quem retirou");
        signatureLabel.setTextSize(16);
        signatureLabel.setTextColor(Color.BLACK);
        signatureLabel.setPadding(0, 24, 0, 12);

        SignatureView signatureView = new SignatureView(this);
        signatureView.setBackgroundColor(Color.WHITE);

        LinearLayout signatureContainer = new LinearLayout(this);
        signatureContainer.setOrientation(LinearLayout.VERTICAL);
        signatureContainer.setPadding(32, 8, 32, 8);
        signatureContainer.addView(inputName);
        signatureContainer.addView(signatureLabel);
        signatureContainer.addView(signatureView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(180)
        ));

        Button buttonClear = new Button(this);
        buttonClear.setText("Limpar assinatura");
        signatureContainer.addView(buttonClear);

        buttonClear.setOnClickListener(v -> signatureView.limpar());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Registrar retirada")
                .setMessage("Informe o nome e solicite a assinatura da pessoa.")
                .setView(signatureContainer)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar retirada", null)
                .create();

        dialog.setOnShowListener(ignored ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {

                            String receiverName = inputName.getText().toString().trim();

                            if (receiverName.isEmpty()) {
                                inputName.setError("Informe o nome de quem retirou.");
                                inputName.requestFocus();
                                return;
                            }

                            if (!signatureView.temAssinatura()) {
                                Toast.makeText(
                                        this,
                                        "Solicite a assinatura antes de confirmar.",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }

                            registrarRetirada(
                                    parcelId,
                                    receiverName,
                                    signatureView,
                                    dialog,
                                    buttonClear
                            );
                        }));

        dialog.show();
    }

    private void registrarRetirada(
            String parcelId,
            String receiverName,
            SignatureView signatureView,
            AlertDialog dialog,
            Button buttonClear) {

        if (auth.getCurrentUser() == null) {
            Toast.makeText(
                    this,
                    "Sua sessão expirou. Faça login novamente.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        buttonClear.setEnabled(false);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        Bitmap signatureBitmap;

        try {
            signatureBitmap = signatureView.criarBitmap();
        } catch (Exception e) {
            buttonClear.setEnabled(true);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

            Toast.makeText(
                    this,
                    "Não foi possível capturar a assinatura: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        File signaturesDirectory = new File(getFilesDir(), "signatures");
        File signatureFile = new File(signaturesDirectory, parcelId + ".png");

        String receiverUid = auth.getCurrentUser().getUid();
        String signaturePath = "signatures/" + parcelId + ".png";

        signatureExecutor.execute(() -> {

            boolean saved = false;
            String errorMessage = null;

            try {
                if (!signaturesDirectory.exists() && !signaturesDirectory.mkdirs()) {
                    throw new IOException("Não foi possível criar a pasta de assinaturas.");
                }

                try (FileOutputStream outputStream = new FileOutputStream(signatureFile)) {
                    saved = signatureBitmap.compress(
                            Bitmap.CompressFormat.PNG,
                            100,
                            outputStream
                    );

                    outputStream.flush();
                }

                if (!saved) {
                    throw new IOException("Não foi possível converter a assinatura em PNG.");
                }

            } catch (Exception e) {
                errorMessage = e.getMessage();

                if (signatureFile.exists()) {
                    signatureFile.delete();
                }
            }

            String finalErrorMessage = errorMessage;
            boolean finalSaved = saved;

            runOnUiThread(() -> {

                signatureBitmap.recycle();

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (!finalSaved) {
                    buttonClear.setEnabled(true);
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

                    Toast.makeText(
                            this,
                            "Erro ao salvar assinatura: " + finalErrorMessage,
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                Map<String, Object> update = new HashMap<>();
                update.put("status", "delivered");
                update.put("receiverName", receiverName);
                update.put("deliveredBy", receiverUid);
                update.put("deliveredAt", FieldValue.serverTimestamp());
                update.put("signaturePath", signaturePath);

                db.collection("parcels")
                        .document(parcelId)
                        .update(update)
                        .addOnSuccessListener(unused -> {

                            dialog.dismiss();

                            Toast.makeText(
                                    this,
                                    "Retirada registrada com assinatura!",
                                    Toast.LENGTH_LONG
                            ).show();

                            carregarEncomendasPendentes();
                            carregarHistoricoEncomendas();
                        })
                        .addOnFailureListener(e -> {

                            signatureExecutor.execute(() -> {
                                if (signatureFile.exists()) {
                                    signatureFile.delete();
                                }
                            });

                            buttonClear.setEnabled(true);
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

                            Toast.makeText(
                                    this,
                                    "Erro ao registrar retirada: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        });
            });
        });
    }

    private LinearLayout criarCard() {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20, 18, 20, 18);
        card.setBackgroundColor(0xFFF1F3F4);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        cardParams.setMargins(0, 0, 0, 16);
        card.setLayoutParams(cardParams);

        return card;
    }

    private String formatarData(Timestamp timestamp) {

        if (timestamp == null) {
            return "Aguardando data";
        }

        Date date = timestamp.toDate();

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd/MM/yyyy 'às' HH:mm",
                new Locale("pt", "BR")
        );

        return dateFormat.format(date);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void adicionarTextoAoCard(
            LinearLayout card,
            String texto,
            boolean destaque) {

        TextView textView = new TextView(this);
        textView.setText(texto);
        textView.setTextSize(15);
        textView.setTextColor(0xFF202124);
        textView.setPadding(0, 0, 0, 8);

        if (destaque) {
            textView.setTextSize(17);
            textView.setTypeface(null, android.graphics.Typeface.BOLD);
        }

        card.addView(textView);
    }

    private String valorOuTraco(String valor) {
        return valor == null || valor.trim().isEmpty() ? "Não informado" : valor;
    }

    private static class SignatureView extends View {

        private final Paint paint;
        private final Path path;
        private boolean hasSignature;

        public SignatureView(android.content.Context context) {
            super(context);

            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5f);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);

            path = new Path();
            hasSignature = false;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            canvas.drawColor(Color.WHITE);
            canvas.drawPath(path, paint);

            Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            linePaint.setColor(0xFFBDBDBD);
            linePaint.setStrokeWidth(2f);

            canvas.drawLine(
                    12,
                    getHeight() - 20,
                    getWidth() - 12,
                    getHeight() - 20,
                    linePaint
            );
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    path.moveTo(x, y);
                    hasSignature = true;
                    invalidate();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    path.lineTo(x, y);
                    invalidate();
                    return true;

                case MotionEvent.ACTION_UP:
                    path.lineTo(x, y);
                    invalidate();
                    return true;

                default:
                    return false;
            }
        }

        public void limpar() {
            path.reset();
            hasSignature = false;
            invalidate();
        }

        public boolean temAssinatura() {
            return hasSignature;
        }

        public Bitmap criarBitmap() {

            Bitmap bitmap = Bitmap.createBitmap(
                    getWidth(),
                    getHeight(),
                    Bitmap.Config.ARGB_8888
            );

            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            canvas.drawPath(path, paint);

            return bitmap;
        }
    }

    private static class UnitOption {

        String id;
        String name;

        UnitOption(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name == null ? "" : name;
        }
    }
}