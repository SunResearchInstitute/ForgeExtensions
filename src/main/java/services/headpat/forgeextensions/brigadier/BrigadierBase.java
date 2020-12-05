package services.headpat.forgeextensions.brigadier;

import com.google.common.collect.ObjectArrays;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import lombok.Getter;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import services.headpat.forgeextensions.ColorCode;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BrigadierBase extends CommandBase {
	@Getter
	private final String name;
	protected final CommandDispatcher<ICommandSender> commandDispatcher;

	/**
	 * @param name               Name of command.
	 * @param dispatcherConsumer The consumer to register commands with.
	 */
	public BrigadierBase(String name, Consumer<CommandDispatcher<ICommandSender>> dispatcherConsumer) {
		this.name = name;
		this.commandDispatcher = new CommandDispatcher<>();
		dispatcherConsumer.accept(commandDispatcher);
	}

	@Override
	public final @NotNull String getUsage(@NotNull ICommandSender sender) {
		StringBuilder builder = new StringBuilder();
		builder.append(ColorCode.RED.getColorCodeString()).append("Usages:").append("\n");
		for (String s : commandDispatcher.getAllUsage(commandDispatcher.getRoot(), sender, true))
			builder.append(ColorCode.RED.getColorCodeString()).append("/").append(s).append("\n");
		return builder.toString();
	}

	@Override
	public final void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args) {
		try {
			int result = this.commandDispatcher.execute(getCommandString(args), sender);
			if (result <= 0) {
				sender.sendMessage(new TextComponentString(getUsage(sender)));
			}
		} catch (CommandSyntaxException e) {
			if (e.getMessage() != null)
				sender.sendMessage(new TextComponentString(ColorCode.RED.getColorCodeString() + e.getMessage()));

			sender.sendMessage(new TextComponentString(getUsage(sender)));
		}
	}

	@Override
	public final @NotNull List<String> getTabCompletions(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args, @Nullable BlockPos targetPos) {
		String commandString = getCommandString(args);
		Suggestions suggestions = this.commandDispatcher.getCompletionSuggestions(this.commandDispatcher.parse(commandString, sender)).join();
		return suggestions.getList().stream().map(Suggestion::getText).collect(Collectors.toList());
	}

	//The way they implement execute doesnt allow for BrigadierBase to work with aliases :(.
	@Override
	public final @NotNull List<String> getAliases() {
		return Collections.emptyList();
	}

	public final String getCommandString(String[] args) {
		return String.join(" ", ObjectArrays.concat(this.getName(), args));
	}
}
