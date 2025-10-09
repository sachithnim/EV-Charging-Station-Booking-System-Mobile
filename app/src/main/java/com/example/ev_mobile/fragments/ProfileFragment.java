package com.example.ev_mobile.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ev_mobile.R;
import com.example.ev_mobile.activities.LoginActivity;
import com.example.ev_mobile.databinding.FragmentProfileBinding;
import com.example.ev_mobile.services.ProfileService;

import org.json.JSONObject;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileService profileService;
    private String nicIdentifier;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        profileService = new ProfileService(requireContext());

        // Load Profile
        loadProfile();

        // Update button
        binding.btnUpdate.setOnClickListener(v -> updateProfile());

        // Deactivate button
        binding.btnDeactivate.setOnClickListener(v -> deactivateAccount());

        // Change password button
        binding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        // Logout button
        binding.btnLogout.setOnClickListener(v -> {
            requireActivity().getSharedPreferences("prefs", requireContext().MODE_PRIVATE)
                    .edit().clear().apply();

            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return binding.getRoot();
    }

    private void loadProfile() {
        profileService.getProfile(new ProfileService.ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject obj = new JSONObject(result);
                    nicIdentifier = obj.optString("identifier");
                    binding.etNic.setText(nicIdentifier);
                    binding.chipRole.setText(obj.optString("role"));
                    binding.etName.setText(obj.optString("name"));
                    binding.etEmail.setText(obj.optString("email"));
                    binding.etPhone.setText(obj.optString("phone"));
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Failed to parse profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile() {
        try {
            JSONObject updatedData = new JSONObject();
            updatedData.put("NIC", nicIdentifier);
            updatedData.put("Name", binding.etName.getText().toString());
            updatedData.put("Email", binding.etEmail.getText().toString());
            updatedData.put("Phone", binding.etPhone.getText().toString());
            updatedData.put("IsActive", true);

            profileService.updateProfile(nicIdentifier, updatedData, new ProfileService.ApiCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void deactivateAccount() {
        profileService.deactivateAccount(nicIdentifier, new ProfileService.ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(requireContext(), "Account deactivated successfully", Toast.LENGTH_SHORT).show();
                requireActivity().getSharedPreferences("prefs", requireContext().MODE_PRIVATE)
                        .edit().clear().apply();

                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                requireActivity().finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Change Password");

        // Create input fields container
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * requireContext().getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        // Old password
        final EditText oldPasswordInput = new EditText(requireContext());
        oldPasswordInput.setHint("Current Password");
        oldPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(oldPasswordInput);

        // New password
        final EditText newPasswordInput = new EditText(requireContext());
        newPasswordInput.setHint("New Password");
        newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPasswordInput);

        // Confirm new password
        final EditText confirmPasswordInput = new EditText(requireContext());
        confirmPasswordInput.setHint("Confirm New Password");
        confirmPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(confirmPasswordInput);

        builder.setView(layout);

        // We set a null listener here and override the button later so we can prevent dialog dismissal on invalid input
        builder.setPositiveButton("Change", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Override positive button to perform validation before dismissing
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String oldPass = oldPasswordInput.getText().toString().trim();
            String newPass = newPasswordInput.getText().toString().trim();
            String confirmPass = confirmPasswordInput.getText().toString().trim();

            if (nicIdentifier == null || nicIdentifier.isEmpty()) {
                Toast.makeText(requireContext(), "Profile not loaded yet.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(requireContext(), "New password and confirmation do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPass.length() < 6) { // example minimum length rule
                Toast.makeText(requireContext(), "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Build JSON and call service
            try {
                JSONObject body = new JSONObject();
                body.put("oldPassword", oldPass);
                body.put("newPassword", newPass);

                // Call change password endpoint
                profileService.changePassword(nicIdentifier, body, new ProfileService.ApiCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // avoid memory leaks
    }
}