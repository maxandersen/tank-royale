using System;
using System.Collections.Generic;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Abstract bot class that takes care of communication between the bot and the server, and sends
  /// notifications through the event handlers. Most bots can inherit from this class to get access
  /// to basic methods.
  /// </summary>
  public partial class BaseBot : IBaseBot
  {
    internal readonly BaseBotInternals __baseBotInternals;

    /// <summary>
    /// Constructor for initializing a new instance of the BaseBot class, which should be used when
    /// both BotInfo and server URI is provided through environment variables, i.e., when starting
    /// up the bot using a bootstrap. These environment variables must be set to provide the server
    /// URL and bot information, and are automatically set by the bootstrap tool for Robocode.
    ///
    /// Example of how to set the predefined environment variables:
    ///
    /// ROBOCODE_SERVER_URI=ws://localhost:55000<br/>
    /// BOT_NAME=MyBot<br/>
    /// BOT_VERSION=1.0<br/>
    /// BOT_AUTHOR=fnl<br/>
    /// BOT_DESCRIPTION=Sample bot<br/>
    /// BOT_URL=https://mybot.robocode.dev<br/>
    /// BOT_COUNTRY_CODE=DK<br/>
    /// BOT_GAME_TYPES=melee,1v1<br/>
    /// BOT_PROG_PLATFORM=.Net Core 3.1<br/>
    /// BOT_PROG_LANG=C# 8<br/>
    /// </summary>
    public BaseBot()
    {
      __baseBotInternals = new BaseBotInternals(this, null, null);
    }

    /// <summary>
    /// Constructor for initializing a new instance of the BaseBot class, which should be used when
    /// server URI is provided through the environment variable ROBOCODE_SERVER_URL.
    /// </summary>
    /// <param name="botInfo">Is the bot info containing information about your bot.</param>
    public BaseBot(BotInfo botInfo)
    {
      __baseBotInternals = new BaseBotInternals(this, botInfo, null);
    }

    /// <summary>
    /// Constructor for initializing a new instance of the BaseBot class, which should be used providing
    /// both the bot information and server URL for your bot.
    /// </summary>
    /// <param name="botInfo">Is the bot info containing information about your bot.</param>
    /// <param name="serverUrl">Is the server URI</param>
    public BaseBot(BotInfo botInfo, Uri serverUrl)
    {
      __baseBotInternals = new BaseBotInternals(this, botInfo, serverUrl);
    }

    /// <inheritdoc/>
    public void Start()
    {
      __baseBotInternals.Connect();
      __baseBotInternals.exitEvent.WaitOne();
    }

    /// <inheritdoc/>
    public void Go()
    {
      __baseBotInternals.SendIntent();
    }

    /// <inheritdoc/>
    public String Variant
    {
      get => __baseBotInternals.ServerHandshake.Variant;
    }

    /// <inheritdoc/>
    public String Version
    {
      get => __baseBotInternals.ServerHandshake.Version;
    }

    /// <inheritdoc/>
    public int MyId
    {
      get => __baseBotInternals.MyId;
    }

    /// <inheritdoc/>
    public String GameType
    {
      get => __baseBotInternals.GameSetup.GameType;
    }

    /// <inheritdoc/>
    public int ArenaWidth
    {
      get => __baseBotInternals.GameSetup.ArenaWidth;
    }

    /// <inheritdoc/>
    public int ArenaHeight
    {
      get => __baseBotInternals.GameSetup.ArenaHeight;
    }

    /// <inheritdoc/>
    public int NumberOfRounds
    {
      get => __baseBotInternals.GameSetup.NumberOfRounds;
    }

    /// <inheritdoc/>
    public double GunCoolingRate
    {
      get => __baseBotInternals.GameSetup.GunCoolingRate;
    }

    /// <inheritdoc/>
    public int? MaxInactivityTurns
    {
      get => __baseBotInternals.GameSetup.MaxInactivityTurns;
    }

    /// <inheritdoc/>
    public int TurnTimeout
    {
      get => __baseBotInternals.GameSetup.TurnTimeout;
    }

    /// <inheritdoc/>
    public int TimeLeft
    {
      get
      {
        long passesMicroSeconds = (DateTime.Now.Ticks - __baseBotInternals.TicksStart) / 10;
        return (int)(__baseBotInternals.GameSetup.TurnTimeout - passesMicroSeconds);
      }
    }

    /// <inheritdoc/>
    public int RoundNumber
    {
      get => __baseBotInternals.CurrentTurn.RoundNumber;
    }

    /// <inheritdoc/>
    public int TurnNumber
    {
      get => __baseBotInternals.CurrentTurn.TurnNumber;
    }

    /// <inheritdoc/>
    public double Energy
    {
      get => __baseBotInternals.CurrentTurn.BotState.Energy;
    }

    /// <inheritdoc/>
    public bool IsDisabled
    {
      get => Energy == 0;
    }

    /// <inheritdoc/>
    public double X
    {
      get => __baseBotInternals.CurrentTurn.BotState.X;
    }

    /// <inheritdoc/>
    public double Y
    {
      get => __baseBotInternals.CurrentTurn.BotState.Y;
    }

    /// <inheritdoc/>
    public double Direction
    {
      get => __baseBotInternals.CurrentTurn.BotState.Direction;
    }

    /// <inheritdoc/>
    public double GunDirection
    {
      get => __baseBotInternals.CurrentTurn.BotState.GunDirection;
    }

    /// <inheritdoc/>
    public double RadarDirection
    {
      get => __baseBotInternals.CurrentTurn.BotState.RadarDirection;
    }

    /// <inheritdoc/>
    public double Speed
    {
      get => __baseBotInternals.CurrentTurn.BotState.Speed;
    }

    /// <inheritdoc/>
    public double GunHeat
    {
      get => __baseBotInternals.CurrentTurn.BotState.GunHeat;
    }

    /// <inheritdoc/>
    public IEnumerable<BulletState> BulletStates
    {
      get => __baseBotInternals.CurrentTurn.BulletStates;
    }

    /// <inheritdoc/>
    public IEnumerable<Event> Events
    {
      get => __baseBotInternals.CurrentTurn.Events;
    }

    /// <inheritdoc/>
    public double TurnRate
    {
      set
      {
        if (Double.IsNaN(value))
        {
          throw new ArgumentException("TurnRate cannot be NaN");
        }
        if (Math.Abs(value) > ((IBaseBot)this).MaxTurnRate)
        {
          value = ((IBaseBot)this).MaxTurnRate * (value > 0 ? 1 : -1);
        }
        __baseBotInternals.BotIntent.TurnRate = value;
      }
      get => __baseBotInternals.BotIntent.TurnRate ?? 0d;
    }

    /// <inheritdoc/>
    public double GunTurnRate
    {
      set
      {
        if (Double.IsNaN(value))
        {
          throw new ArgumentException("GunTurnRate cannot be NaN");
        }
        if (IsAdjustGunForBodyTurn)
        {
          value -= value;
        }
        if (Math.Abs(value) > ((IBaseBot)this).MaxGunTurnRate)
        {
          value = ((IBaseBot)this).MaxGunTurnRate * (value > 0 ? 1 : -1);
        }
        __baseBotInternals.BotIntent.GunTurnRate = value;
      }
      get => __baseBotInternals.BotIntent.GunTurnRate ?? 0d;
    }

    /// <inheritdoc/>
    public double RadarTurnRate
    {
      set
      {
        if (Double.IsNaN(value))
        {
          throw new ArgumentException("RadarTurnRate cannot be NaN");
        }
        if (IsAdjustRadarForGunTurn)
        {
          value -= value;
        }
        if (Math.Abs(value) > ((IBaseBot)this).MaxRadarTurnRate)
        {
          value = ((IBaseBot)this).MaxRadarTurnRate * (value > 0 ? 1 : -1);
        }
        __baseBotInternals.BotIntent.RadarTurnRate = value;
      }
      get => __baseBotInternals.BotIntent.RadarTurnRate ?? 0d;
    }

    /// <inheritdoc/>
    public double TargetSpeed
    {
      set
      {
        if (Double.IsNaN(value))
        {
          throw new ArgumentException("TargetSpeed cannot be NaN");
        }
        if (value > ((IBaseBot)this).MaxForwardSpeed)
        {
          value = ((IBaseBot)this).MaxForwardSpeed;
        }
        else if (value < ((IBaseBot)this).MaxBackwardSpeed)
        {
          value = ((IBaseBot)this).MaxBackwardSpeed;
        }
        __baseBotInternals.BotIntent.TargetSpeed = value;
      }
      get => __baseBotInternals.BotIntent.TargetSpeed ?? 0d;
    }

    /// <inheritdoc/>
    public double Firepower
    {
      set
      {
        if (Double.IsNaN(value))
        {
          throw new ArgumentException("Firepower cannot be NaN");
        }
        if (GunHeat == 0)
        {
          if (value < ((IBaseBot)this).MinFirepower)
          {
            value = 0;
          }
          else if (value > ((IBaseBot)this).MaxFirepower)
          {
            value = ((IBaseBot)this).MaxFirepower;
          }
          __baseBotInternals.BotIntent.Firepower = value;
        }
      }
      get => __baseBotInternals.BotIntent.Firepower ?? 0d;
    }

    /// <inheritdoc/>
    public bool IsAdjustGunForBodyTurn
    {
      set => __baseBotInternals.isAdjustGunForBodyTurn = value;
      get => __baseBotInternals.isAdjustGunForBodyTurn;
    }

    /// <inheritdoc/>
    public bool IsAdjustRadarForGunTurn
    {
      set => __baseBotInternals.isAdjustRadarForGunTurn = value;
      get => __baseBotInternals.isAdjustRadarForGunTurn;
    }

    /// <inheritdoc/>
    public double CalcMaxTurnRate(double speed)
    {
      return ((IBaseBot)this).MaxTurnRate - 0.75 * Math.Abs(speed);
    }

    /// <inheritdoc/>
    public double CalcBulletSpeed(double firepower)
    {
      return 20 - 3 * firepower;
    }

    /// <inheritdoc/>
    public double CalcGunHeat(double firepower)
    {
      return 1 + (firepower / 5);
    }

    /// <inheritdoc/>
    public virtual void OnConnected(ConnectedEvent connectedEvent) { }

    /// <inheritdoc/>
    public virtual void OnDisconnected(DisconnectedEvent disconnectedEvent) { }

    /// <inheritdoc/>
    public virtual void OnConnectionError(ConnectionErrorEvent connectionErrorEvent) { }

    /// <inheritdoc/>
    public virtual void OnGameStarted(GameStartedEvent gameStatedEvent) { }

    /// <inheritdoc/>
    public virtual void OnGameEnded(GameEndedEvent gameEndedEvent) { }

    /// <inheritdoc/>
    public virtual void OnTick(TickEvent tickEvent) { }

    /// <inheritdoc/>
    public virtual void OnBotDeath(BotDeathEvent botDeathEvent) { }

    /// <inheritdoc/>
    public virtual void OnHitBot(BotHitBotEvent botHitBotEvent) { }

    /// <inheritdoc/>
    public virtual void OnHitWall(BotHitWallEvent botHitWallEvent) { }

    /// <inheritdoc/>
    public virtual void OnBulletFired(BulletFiredEvent bulletFiredEvent) { }

    /// <inheritdoc/>
    public virtual void OnHitByBullet(BulletHitBotEvent bulletHitBotEvent) { }

    /// <inheritdoc/>
    public virtual void OnBulletHit(BulletHitBotEvent bulletHitBotEvent) { }

    /// <inheritdoc/>
    public virtual void OnBulletHitBullet(BulletHitBulletEvent bulletHitBulletEvent) { }

    /// <inheritdoc/>
    public virtual void OnBulletHitWall(BulletHitWallEvent bulletHitWallEvent) { }

    /// <inheritdoc/>
    public virtual void OnScannedBot(ScannedBotEvent scannedBotEvent) { }

    /// <inheritdoc/>
    public virtual void OnSkippedTurn(SkippedTurnEvent skippedTurnEvent) { }

    /// <inheritdoc/>
    public virtual void OnWonRound(WonRoundEvent wonRoundEvent) { }
  }
}