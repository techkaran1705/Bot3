package com.vegazsdev.bobobot.commands.gsi;

import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.core.command.Command;
import com.vegazsdev.bobobot.core.gsi.GSICmdObj;
import com.vegazsdev.bobobot.core.gsi.SourceForgeUpload;
import com.vegazsdev.bobobot.db.PrefObj;
import com.vegazsdev.bobobot.utils.Config;
import com.vegazsdev.bobobot.utils.FileTools;
import com.vegazsdev.bobobot.utils.JSONs;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class ErfanGSIs extends Command {

    private static final Logger logger = LoggerFactory.getLogger(ErfanGSIs.class);
    private static final ArrayList<GSICmdObj> queue = new ArrayList<>();
    private static boolean isPorting = false;
    private final String toolPath = "ErfanGSIs/";

    private final File[] supportedGSIs9 = new File(toolPath + "roms/9").listFiles(File::isDirectory);
    private final File[] supportedGSIs10 = new File(toolPath + "roms/10").listFiles(File::isDirectory);
    private final File[] supportedGSIs11 = new File(toolPath + "roms/11").listFiles(File::isDirectory);
    private final File[] supportedGSIs12 = new File(toolPath + "roms/S").listFiles(File::isDirectory);

    private String infoGSI = "";

    public ErfanGSIs() {
        super("jurl2gsi", "Can port gsi");
    }

    private static String[] listFilesForFolder(final File folder) {
        StringBuilder paths = new StringBuilder();
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                if (fileEntry.getName().contains(".img")) {
                    paths.append(fileEntry.getAbsolutePath()).append("\n");
                }
            }
        }
        return paths.toString().split("\n");
    }

    @Override
    public void botReply(Update update, TelegramBot bot, PrefObj prefs) {
        String[] msgComparableRaw = update.getMessage().getText().split(" ");
        String msg = update.getMessage().getText();
        String idAsString = update.getMessage().getFrom().getId().toString();

        if (msgComparableRaw[1].equals("allowuser") && Objects.equals(Config.getDefConfig("bot-master"), idAsString)) {
            if (update.getMessage().getReplyToMessage() != null) {
                String userid = update.getMessage().getReplyToMessage().getFrom().getId().toString();
                if (addPortPerm(userid)) {
                    bot.sendReply(prefs.getString("egsi_allowed").replace("%1", userid), update);
                }
            } else {
                bot.sendReply(prefs.getString("egsi_allow_by_reply").replace("%1", prefs.getHotkey())
                        .replace("%2", this.getAlias()), update);
            }
        } else if (msgComparableRaw[1].equals("queue")) {
            if (!queue.isEmpty()) {
                StringBuilder v = new StringBuilder();
                for (int i = 0; i < queue.size(); i++) {
                    v.append("#").append(i + 1).append(": ").append(queue.get(i).getGsi()).append("\n");
                }
                bot.sendReply(prefs.getString("egsi_current_queue")
                        .replace("%2", v.toString())
                        .replace("%1", String.valueOf(queue.size())), update);
            } else {
                bot.sendReply(prefs.getString("egsi_no_ports_queue"), update);
            }
        } else if (msgComparableRaw[1].equals("cancel")) {

            // cancel by now, maybe work, not tested
            // will exit only on when porting is "active" (when url2gsi.sh is running)
            // after that when port already already finished (eg. uploading, zipping)
            // so this cancel needs more things to fully work

            ProcessBuilder pb;
            pb = new ProcessBuilder("/bin/bash", "-c", "kill -TERM -- -$(ps ax | grep url2GSI.sh | grep -v grep | awk '{print $1;}')");
            try {
                pb.start();
            } catch (IOException ignored) {}

            if (FileTools.checkIfFolderExists(toolPath + "output")) {
                if (FileTools.deleteFolder(toolPath + "output")) {
                    logger.info("Output folder deleted");
                }
            }
        } else {
            if (userHasPortPermissions(idAsString)) {
                if (!FileTools.checkIfFolderExists("ErfanGSIs")) {
                    bot.sendReply(prefs.getString("egsi_dont_exists_tool_folder"), update);
                } else {
                    GSICmdObj gsiCommand = isCommandValid(update);
                    if (gsiCommand != null) {
                        boolean isGSITypeValid = isGSIValid(gsiCommand.getGsi());
                        if (isGSITypeValid) {
                            if (!isPorting) {
                                isPorting = true;
                                createGSI(gsiCommand, bot);
                                while (queue.size() != 0) {
                                    GSICmdObj portNow = queue.get(0);
                                    queue.remove(0);
                                    createGSI(portNow, bot);
                                }
                                isPorting = false;
                            } else {
                                queue.add(gsiCommand);
                                bot.sendReply(prefs.getString("egsi_added_to_queue"), update);
                            }
                        } else {
                            File[] supportedGSIsPandQ = ArrayUtils.addAll(supportedGSIs9, supportedGSIs10);
                            File[] supportedGSIsRandS = ArrayUtils.addAll(supportedGSIs11, supportedGSIs12);

                            if (supportedGSIsPandQ != null && supportedGSIsRandS != null) {
                                bot.sendReply(prefs.getString("egsi_supported_types")
                                        .replace("%1",
                                                Arrays.toString(supportedGSIs9).replace(toolPath + "roms/9/", "")
                                                        .replace("[", "")
                                                        .replace("]", ""))
                                        .replace("%2",
                                                Arrays.toString(supportedGSIs10).replace(toolPath + "roms/10/", "")
                                                        .replace("[", "")
                                                        .replace("]", ""))
                                        .replace("%3",
                                                Arrays.toString(supportedGSIs11).replace(toolPath + "roms/11/", "")
                                                        .replace("[", "")
                                                        .replace("]", ""))
                                        .replace("%4",
                                                Arrays.toString(supportedGSIs12).replace(toolPath + "roms/S/", "")
                                                        .replace("[", "")
                                                        .replace("]", "")), update);
                            } else {
                                bot.sendReply(prefs.getString("egsi_something_is_wrong"), update);
                            }
                        }
                    }
                }
            }
        }
    }

    private String try2AvoidCodeInjection(String parameters) {
        try {
            parameters = parameters.replace("&", "")
                    .replace("\\", "").replace(";", "").replace("<", "")
                    .replace(">", "").replace("|", "");
        } catch (Exception e) {
            return parameters;
        }
        return parameters;
    }

    private GSICmdObj isCommandValid(Update update) {
        GSICmdObj gsiCmdObj = new GSICmdObj();
        String[] msgComparableRaw = update.getMessage().getText().split(" ");
        String msg = update.getMessage().getText().replace(Config.getDefConfig("bot-hotkey") + this.getAlias() + " ", "");
        String url, gsi, param;

        if (msgComparableRaw.length >= 3) {
            try {
                url = msg.split(" ")[1];
                gsiCmdObj.setUrl(url);
                gsi = msg.split(" ")[2];
                gsiCmdObj.setGsi(gsi);
                param = msg.replace(url + " ", "").replace(gsi, "").trim();
                param = try2AvoidCodeInjection(param);
                gsiCmdObj.setParam(param);
                gsiCmdObj.setUpdate(update);
                return gsiCmdObj;
            } catch (Exception e) {
                logger.error(e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    private boolean isGSIValid(String gsi) {
        File[] supportedGSIsPandQ = ArrayUtils.addAll(supportedGSIs9, supportedGSIs10);
        File[] supportedGSIsRandS = ArrayUtils.addAll(supportedGSIs11, supportedGSIs12);

        if (supportedGSIsPandQ == null) return false;

        boolean canRunYet = true;

        try {
            String gsi2 = null;

            if (gsi.contains(":")) {
                gsi2 = gsi.split(":")[0];
            }

            for (File supportedGSI : Objects.requireNonNull(supportedGSIsPandQ)) {
                canRunYet = false;
                if (gsi2 != null) {
                    if (gsi2.equals(supportedGSI.getName())) return true;
                } else {
                    if (gsi.equals(supportedGSI.getName())) return true;
                }
            }

            if (canRunYet) {
                for (File supportedGSI : Objects.requireNonNull(supportedGSIsRandS)) {
                    if (gsi2 != null) {
                        if (gsi2.equals(supportedGSI.getName())) return true;
                    } else {
                        if (gsi.equals(supportedGSI.getName())) return true;
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
        return false;
    }

    private boolean userHasPortPermissions(String idAsString) {
        if (Objects.equals(Config.getDefConfig("bot-master"), idAsString)) {
            return true;
        }
        String portConfigFile = "configs/allowed2port.json";
        return Arrays.asList(Objects.requireNonNull(JSONs.getArrayFromJSON(portConfigFile)).toArray()).contains(idAsString);
    }

    private String getModelOfOutput() {
        StringBuilder fullLogs = new StringBuilder();

        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;

        try {
            ProcessBuilder processBuilder;
            processBuilder = new ProcessBuilder("/bin/bash", "-c",
                    "grep -oP \"(?<=^Model: ).*\" -hs \"$(pwd)\"/ErfanGSIs/output/*txt | head -1"
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            inputStream = process.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);

            String line;

            /*
             * Get codename
             */
            while ((line = bufferedReader.readLine()) != null) {
                if (line.length() < 1)
                    line = "Generic";
                else if (line.toLowerCase().contains("x00qd"))
                    line = "Asus Zenfone 5";
                else if (line.toLowerCase().contains("qssi"))
                    line = "Qualcomm Single System Image";
                else if (line.toLowerCase().contains("miatoll"))
                    line = "MiAtoll";
                else if (line.toLowerCase().contains("surya"))
                    line = "Poco X3";
                else if (line.toLowerCase().contains("mainline"))
                    line = "AOSP/Pixel (Mainline) Device";
                else if (line.toLowerCase().contains("sm6250"))
                    line = "Atoll device";
                else if (line.toLowerCase().contains("msi"))
                    line = "Motorola System Image";
                else if (line.toLowerCase().contains("mssi"))
                    line = "MIUI Single System Image";
                else if (line.toLowerCase().contains("a30"))
                    line = "Samsung Galaxy A30";
                else if (line.toLowerCase().contains("a20"))
                    line = "Samsung Galaxy A20";
                else if (line.toLowerCase().contains("a10"))
                    line = "Samsung Galaxy A10";
                else if (line.equals(" "))
                    line = "Generic";

                fullLogs.append(line);
            }

            /*
             * First check
             */
            String stringToBeCheked = fullLogs.toString().toLowerCase();
            boolean testPass = false;

            char[] characterSearch = {
                    'q', 'w', 'e', 'r', 't', 'y', 'u',
                    'i', 'o', 'p', 'a', 's', 'd', 'f',
                    'g', 'h', 'j', 'k', 'l', 'z', 'x',
                    'c', 'v', 'b', 'n', 'm',
            };

            for (int i = 0; i < stringToBeCheked.length(); i++) {
                char character = stringToBeCheked.charAt(i);
                for (char search : characterSearch) {
                    if (search == character) {
                        testPass = true;
                        break;
                    }
                }
            }

            /*
             * Second check
             */
            if (!testPass) return "Generic";
            return String.valueOf(fullLogs);
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioException) {
                    logger.error(ioException.getMessage(), ioException);
                }
            }

            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException ioException) {
                    logger.error(ioException.getMessage(), ioException);
                }
            }

            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ioException) {
                    logger.error(ioException.getMessage(), ioException);
                }
            }
        }
        return "Generic";
    }

    private void createGSI(GSICmdObj gsiCmdObj, TelegramBot bot) {
        Update update = gsiCmdObj.getUpdate();
        ProcessBuilder pb;

        pb = new ProcessBuilder("/bin/bash", "-c",
                "cd " + toolPath + " ; ./url2GSI.sh '" + gsiCmdObj.getUrl() + "' " + gsiCmdObj.getGsi() + " " + gsiCmdObj.getParam()
        );

        boolean success = false;

        StringBuilder fullLogs = new StringBuilder();
        fullLogs.append("<code>-> Starting process...</code>");

        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;

        int id = bot.sendReply(fullLogs.toString(), update);

        try {
            pb.redirectErrorStream(true);
            Process process = pb.start();

            inputStream = process.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);

            String line;

            boolean weDontNeedAria2Logs = true;

            while ((line = bufferedReader.readLine()) != null) {
                line = "<code>" + line + "</code>";
                if (line.contains("Downloading firmware to:")) {
                    weDontNeedAria2Logs = false;
                    fullLogs.append("\n").append(line);
                    bot.editMessage(fullLogs.toString(), update, id);
                }

                if (line.contains("Create Temp and out dir")) {
                    weDontNeedAria2Logs = true;
                }

                if (weDontNeedAria2Logs) {
                    fullLogs.append("\n").append(line);
                    bot.editMessage(fullLogs.toString(), update, id);
                    if (line.contains("GSI done on:")) {
                        success = true;
                    }
                }
            }

            if (success) {
                fullLogs.append("\n").append("<code>Creating gzip...</code>");
                bot.editMessage(fullLogs.toString(), update, id);

                String[] gzipFiles = listFilesForFolder(new File("ErfanGSIs" + "/output"));
                for (String gzipFile : gzipFiles) {
                    new FileTools().gzipFile(gzipFile, gzipFile + ".gz");
                }

                ArrayList<String> arr = new ArrayList<>();

                AtomicReference<String> aonly = new AtomicReference<>("");
                AtomicReference<String> ab = new AtomicReference<>("");

                try (Stream<Path> paths = Files.walk(Paths.get("ErfanGSIs/output/"))) {
                    paths
                            .filter(Files::isRegularFile)
                            .forEach(fileName -> {
                                if (fileName.toString().endsWith(".img.gz")) {
                                    arr.add(fileName.toString());
                                    if (fileName.toString().contains("Aonly")) {
                                        aonly.set(FilenameUtils.getBaseName(fileName.toString()) + "." + FilenameUtils.getExtension(fileName.toString()));
                                    } else {
                                        ab.set(FilenameUtils.getBaseName(fileName.toString()) + "." + FilenameUtils.getExtension(fileName.toString()));
                                    }
                                }
                                if (fileName.toString().contains(".txt")) {
                                    infoGSI = fileName.toString();
                                }
                            });
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }

                fullLogs.append("\n").append("<code>Sending files to SF...</code>");
                bot.editMessage(fullLogs.toString(), update, id);

                String re = new SourceForgeUpload().uploadGsi(arr, gsiCmdObj.getGsi());
                re = re + "/";

                if (gsiCmdObj.getGsi().contains(":")) {
                    gsiCmdObj.setGsi(gsiCmdObj.getGsi().split(":")[1]);
                    gsiCmdObj.setGsi(gsiCmdObj.getGsi().replace("-", " "));
                }

                /*
                 * Prepare GSI message
                 */
                SendMessage sendMessage = new SendMessage();
                sendMessage.setDisableWebPagePreview(true);
                sendMessage.enableHtml(true);

                /*
                 * Prepare InlineKeyboardButton
                 */
                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

                if (!aonly.toString().trim().equals("")) {
                    List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
                    InlineKeyboardButton inlineKeyboardButtonAonly = new InlineKeyboardButton();
                    inlineKeyboardButtonAonly.setText("Aonly Download");
                    inlineKeyboardButtonAonly.setUrl("https://sourceforge.net/projects/" + SourceForgeSetup.getSfConf("bot-sf-proj") + "/files/" + re + aonly);
                    rowInline2.add(inlineKeyboardButtonAonly);
                    rowsInline.add(rowInline2);
                }

                if (!ab.toString().trim().equals("")) {
                    List<InlineKeyboardButton> rowInline = new ArrayList<>();
                    InlineKeyboardButton inlineKeyboardButtonAB = new InlineKeyboardButton();
                    inlineKeyboardButtonAB.setText("A/B Download");
                    inlineKeyboardButtonAB.setUrl("https://sourceforge.net/projects/" + SourceForgeSetup.getSfConf("bot-sf-proj") + "/files/" + re + ab);
                    rowInline.add(inlineKeyboardButtonAB);
                    rowsInline.add(rowInline);
                }

                /*
                 * Finish InlineKeyboardButton setup
                 */
                markupInline.setKeyboard(rowsInline);
                sendMessage.setReplyMarkup(markupInline);

                /*
                 * Info of GSI image
                 */
                String descGSI = "" + new FileTools().readFile(infoGSI).trim();

                /*
                 * Reply the sucess of the build
                 */
                bot.sendReply("Done!", update);

                /*
                 * Send GSI message
                 */
                sendMessage.setText("<b>Requested " + gsiCmdObj.getGsi() + " GSI</b>"
                        + "\n<b>From</b> " + getModelOfOutput()
                        + "\n\n<b>Information</b>\n<code>" + descGSI
                        + "</code>\n\n<b>Credits</b>" + "\n"
                        + "<a href=\"https://github.com/Erfanoabdi\">Erfan Abdi</a>" + " | "
                        + "<a href=\"https://github.com/TrebleExperience/Bot3\">Bo³+t</a>" + "\n\n"
                        + "<b>Treble Experience</b>" + "\n"
                        + "<a href=\"https://t.me/TrebleExperience\">Channel</a> | <a href=\"https://t.me/TrebleExperience_chat\">Chat</a> | <a href=\"https://github.com/TrebleExperience\">GitHub</a>"
                );
                sendMessage.setChatId(Objects.requireNonNull(SourceForgeSetup.getSfConf("bot-announcement-id")));
                bot.sendMessageAsyncBase(sendMessage, update);

                fullLogs.append("\n").append("Finished!");
                bot.editMessage(fullLogs.toString(), update, id);
                FileUtils.deleteDirectory(new File(toolPath + "output"));
            } else {
                throw new Exception("Task finished without generating a valid GSI");
            }
        } catch (Exception ex) {
            logger.error(String.valueOf(fullLogs));
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioException) {
                    logger.error(ioException.getMessage());
                }
            }

            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException ioException) {
                    logger.error(ioException.getMessage());
                }
            }

            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ioException) {
                    logger.error(ioException.getMessage());
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean addPortPerm(String id) {
        try {
            if (FileTools.checkFileExistsCurPath("configs/allowed2port.json")) {
                ArrayList arrayList = JSONs.getArrayFromJSON("configs/allowed2port.json");
                if (arrayList != null) {
                    arrayList.add(id);
                }
                JSONs.writeArrayToJSON(arrayList, "configs/allowed2port.json");
            } else {
                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.add(id);
                JSONs.writeArrayToJSON(arrayList, "configs/allowed2port.json");
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }
}