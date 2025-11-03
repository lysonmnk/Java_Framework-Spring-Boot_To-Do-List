package org.delcom.starter.controllers;

import java.lang.reflect.Method;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HomeControllerTest {

    private HomeController controller;

    @BeforeEach
    void setUp() {
        controller = new HomeController();
    }

    // --- Tes untuk 1. informasiNim ---
    @Test
    void testInformasiNim_Valid() {
        String result = controller.informasiNim("11S23001");
        assertTrue(result.contains("Sarjana Informatika"));
        assertTrue(result.contains("Angkatan: 2023"));
        assertTrue(result.contains("Urutan: 1"));
    }

    @Test
    void testInformasiNim_InvalidAndUnknown() {
        assertTrue(controller.informasiNim("123").contains("minimal 8 karakter"));
        assertTrue(controller.informasiNim(null).contains("minimal 8 karakter"));
        assertTrue(controller.informasiNim("99X23123").contains("Unknown"));
    }

    // --- Tes untuk 2. perolehanNilai ---
    @Test
    void testPerolehanNilai_Valid() {
        String data = "UAS|85|40\nUTS|75|30\nPA|90|20\nK|100|10";
        String b64 = Base64.getEncoder().encodeToString(data.getBytes());
        String result = controller.perolehanNilai(b64);
        assertTrue(result.contains("84.50"));
        assertTrue(result.contains("Total Bobot: 100%"));
        assertTrue(result.contains("Grade: B"));
    }

    @Test
    void testPerolehanNilai_BranchCoverage() {
        // Test case ini dirancang untuk mencakup semua cabang logika yang terlewat
        String data = 
            "UAS|90|50\n" +         // Baris valid
            "\n" +                  // Baris kosong
            "Tugas|80|0\n" +        // Baris dengan bobot 0
            "Invalid Line\n" +      // Baris tanpa '|'
            "Hanya|Dua\n" +         // Baris dengan format salah (hanya 2 bagian)
            "Nilai|abc|def\n" +     // Baris dengan format angka salah
            "---\n";
        String b64 = Base64.getEncoder().encodeToString(data.getBytes());
        String result = controller.perolehanNilai(b64);
        assertEquals("Nilai Akhir: 45.00 (Total Bobot: 50%)\nGrade: E", result);
    }

    @Test
    void testPerolehanNilai_InvalidBase64() {
        assertThrows(IllegalArgumentException.class, () -> controller.perolehanNilai("!@#"));
    }

    // --- Tes untuk 3. perbedaanL ---
    @Test
    void testPerbedaanL_Valid() {
        String b64 = Base64.getEncoder().encodeToString("UULL".getBytes());
        String result = controller.perbedaanL(b64);
        assertTrue(result.contains("UULL -> (-2, 2)"));
        assertTrue(result.contains("DDRR -> (2, -2)"));
        assertTrue(result.contains("Perbedaan Jarak: 8"));
    }

    @Test
    void testPerbedaanL_WithInvalidChars() {
        // Karakter 'X' dan spasi akan diabaikan oleh switch-case, ini untuk coverage
        String path = "U L X R D";
        String b64 = Base64.getEncoder().encodeToString(path.getBytes());
        String result = controller.perbedaanL(b64);
        // Hasilnya sama seperti "ULRD" -> (0,0) dan kebalikannya "DRLU" -> (0,0)
        assertTrue(result.contains(String.format("Path Original: %s -> (0, 0)", path)));
        assertTrue(result.contains("Path Kebalikan: D R L U -> (0, 0)"));
        assertTrue(result.contains("Perbedaan Jarak: 0"));
    }

    // --- Tes untuk 4. palingTer ---
    @Test
    void testPalingTer_Valid() {
        String text = "terbaik Terbaik terbaik termahal termahal";
        String b64 = Base64.getEncoder().encodeToString(text.getBytes());
        String result = controller.palingTer(b64);
        assertTrue(result.contains("'terbaik' (muncul 3 kali)"));
    }

    @Test
    void testPalingTer_BranchCoverage() {
        // Mencakup kasus tidak ditemukan kata 'ter'
        String b64 = Base64.getEncoder().encodeToString("hello world".getBytes());
        assertEquals("Tidak ditemukan kata yang berawalan 'ter'.", controller.palingTer(b64));

        // Mencakup kasus string kosong
        String b64Empty = Base64.getEncoder().encodeToString("".getBytes());
        assertEquals("Tidak ditemukan kata yang berawalan 'ter'.", controller.palingTer(b64Empty));
    }
    
    @Test
    void testPalingTer_AnotherWinner() {
        String text = "terbaik termahal terpendek termahal";
        String b64 = Base64.getEncoder().encodeToString(text.getBytes());
        String result = controller.palingTer(b64);
        assertTrue(result.contains("'termahal' (muncul 2 kali)"));
    }

    // --- Tes untuk Helper Method (calculateGrade) ---
    @Test
    void testCalculateGrade_Coverage() throws Exception {
        Method method = HomeController.class.getDeclaredMethod("calculateGrade", double.class);
        method.setAccessible(true);
        assertEquals("A", method.invoke(controller, 90.0)); // nilai >= 85
        assertEquals("B", method.invoke(controller, 80.0)); // nilai >= 75
        assertEquals("C", method.invoke(controller, 70.0)); // nilai >= 65
        assertEquals("D", method.invoke(controller, 60.0)); // nilai >= 55
        assertEquals("E", method.invoke(controller, 50.0)); // else
    }
}