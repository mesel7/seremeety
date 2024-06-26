package com.example.seremeety.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.seremeety.R;

public class DialogUtils {
    public static void showConfirmationDialog(Context context, String title, String message) {
        showDialogInternal(
                context,
                title,
                message,
                false, // 확인 버튼만 보여줌
                (dialog, which) -> dialog.dismiss(),
                null,
                null
        );
    }

    public static void showDialog(Context context, String title, String message,
                                  DialogInterface.OnClickListener positiveButtonListener,
                                  DialogInterface.OnClickListener negativeButtonListener) {
        showDialogInternal(
                context,
                title,
                message,
                true, // 취소 버튼도 보여줌
                positiveButtonListener,
                negativeButtonListener,
                null
        );
    }

    public static void showAcceptOrRejectDialog(Context context, String title, String message,
                                                DialogInterface.OnClickListener acceptButtonListener,
                                                DialogInterface.OnClickListener rejectButtonListener) {
        showDialogInternal(
                context,
                title,
                message,
                true, // 취소 버튼도 보여줌
                acceptButtonListener,
                rejectButtonListener,
                (dialog, which) -> dialog.dismiss() // 닫기
        );
    }

    private static void showDialogInternal(Context context, String title, String message, boolean showNegativeButton,
                                           DialogInterface.OnClickListener positiveButtonListener,
                                           DialogInterface.OnClickListener negativeButtonListener,
                                           DialogInterface.OnClickListener closeButtonListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.alert_dialog, null);
        builder.setView(dialogView);

        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        TextView dialogButtonBar = dialogView.findViewById(R.id.dialog_button_bar);
        Button positiveButton = dialogView.findViewById(R.id.positive_button);
        Button negativeButton = dialogView.findViewById(R.id.negative_button);
        Button closeButton = dialogView.findViewById(R.id.close_button);

        dialogTitle.setText(title);
        dialogMessage.setText(message);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        if (closeButtonListener != null) {
            negativeButton.setText("거절");
            positiveButton.setText("수락");
            closeButton.setVisibility(View.VISIBLE);
            closeButton.setOnClickListener(v -> {
                closeButtonListener.onClick(dialog, DialogInterface.BUTTON_NEUTRAL);
                dialog.dismiss();
            });
        }

        if (showNegativeButton) {
            dialogButtonBar.setVisibility(View.VISIBLE);
            negativeButton.setVisibility(View.VISIBLE);
            negativeButton.setOnClickListener(v -> {
                if (negativeButtonListener != null) {
                    negativeButtonListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                }
                dialog.dismiss();
            });
        }

        positiveButton.setOnClickListener(v -> {
            if (positiveButtonListener != null) {
                positiveButtonListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
            }
            dialog.dismiss();
        });

        dialog.setCancelable(false);
        dialog.show();
    }
}
