package com.fatema.procurement.controller;

import com.fatema.procurement.entity.GoodsReceipt;
import com.fatema.procurement.entity.ReceiptStatus;
import com.fatema.procurement.service.GoodsReceiptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/goods-receipts")
public class GoodsReceiptController {

    private static final Logger logger = LoggerFactory.getLogger(GoodsReceiptController.class);

    @Autowired
    private GoodsReceiptService goodsReceiptService;

    @GetMapping
    public String listReceipts(Model model) {
        try {
            logger.info("=== START listReceipts ===");

            // Проверяем, что сервис не null
            if (goodsReceiptService == null) {
                logger.error("goodsReceiptService is NULL!");
                model.addAttribute("error", "Сервис не инициализирован");
                return "error";
            }

            logger.info("Getting all receipts...");
            List<GoodsReceipt> receipts = goodsReceiptService.getAllReceipts();
            logger.info("Found {} receipts", receipts != null ? receipts.size() : 0);

            model.addAttribute("receipts", receipts != null ? receipts : List.of());
            model.addAttribute("statuses", ReceiptStatus.values());
            model.addAttribute("totalReceipts", receipts != null ? receipts.size() : 0);
            model.addAttribute("pendingReceipts", 0);
            model.addAttribute("confirmedReceipts", 0);
            model.addAttribute("receiptsToday", 0);

            logger.info("Rendering goods-receipts/list");
            return "goods-receipts/list";

        } catch (Exception e) {
            logger.error("ERROR in listReceipts: ", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("stackTrace", e.getStackTrace());
            return "error";
        }
    }
}
