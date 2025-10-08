package com.example.ev_mobile.fragments;

import android.content.Intent;
import android.os.Bundle;
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
        profileService.deactivateAccount(nicIdentifier,new ProfileService.ApiCallback<String>() {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // avoid memory leaks
    }
}